# ──────────────────────────────────────────────────────────────
# Bilhética — Makefile
# Automates dev, k8s deploy, seeding, indexes, and load testing
# ──────────────────────────────────────────────────────────────

# ── Config ────────────────────────────────────────────────────
IMAGE_NAME    := bilhetica-api
IMAGE_TAG     := dev
IMAGE         := $(IMAGE_NAME):$(IMAGE_TAG)

DB_NAME       := bilhetica
DB_USER       := bilhetica
DB_PASS       := bilhetica123

# Local docker-compose postgres is on 5434, k8s port-forward uses 5435
LOCAL_DB_PORT := 5434
K8S_DB_PORT   := 5435

API_PORT      := 8080

# Postgres client (psql/pg_dump/pg_restore) run in a throwaway container so no
# host psql install is needed. --network host reaches published/forwarded ports;
# the repo is mounted at /work so \copy resolves 'data/seed/*.csv' relative paths.
PG_CLIENT_IMG := postgres:16-alpine
DOCKER_PG     := docker run --rm -i --network host -v "$(PWD)":/work -w /work -e PGPASSWORD=$(DB_PASS) $(PG_CLIENT_IMG)

MINIKUBE_MEM  := 12288
MINIKUBE_CPUS := 8

# ── Phony targets ────────────────────────────────────────────
.PHONY: help \
        dev dev-infra dev-api dev-stop api-stop \
        build release \
        k8s-start k8s-deploy k8s-status k8s-tunnel k8s-stop \
        seed-generate seed-local seed-k8s pip-install \
        snapshot-local restore-local snapshot-k8s restore-k8s \
        indexes-local indexes-k8s \
        test-user admin-user \
        k6-fase1 k6-fase2 k6-fase3 k6-validacao k6-all \
        clean clean-local clean-k8s

# ── Default ──────────────────────────────────────────────────
.DEFAULT_GOAL := help

# ══════════════════════════════════════════════════════════════
# HELP
# ══════════════════════════════════════════════════════════════

help: ## Show this help
	@echo ""
	@echo "  Bilhética — available targets"
	@echo "  ─────────────────────────────────────"
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""

# ══════════════════════════════════════════════════════════════
# LOCAL DEVELOPMENT
# ══════════════════════════════════════════════════════════════

api-stop: ## Stop any host-side Spring Boot API process (frees API_PORT)
	@echo "🛑 Stopping any local API on port $(API_PORT)..."
	@-pkill -f "multiModuleProjectDirectory=$(PWD).*spring-boot:run" 2>/dev/null || true
	@-pkill -f "$(PWD)/target/classes.*BilheticaApplication" 2>/dev/null || true
	@PIDS=$$(ss -ltnpH "sport = :$(API_PORT)" 2>/dev/null | grep -o 'pid=[0-9]*' | cut -d= -f2); \
		if [ -n "$$PIDS" ]; then kill $$PIDS 2>/dev/null || true; echo "   freed port $(API_PORT) (pids: $$PIDS)"; fi
	@echo "✅ API stopped"

dev-infra: ## Start PostgreSQL + Redis locally (docker-compose)
	docker compose up postgres redis -d
	@echo "⏳ Waiting for PostgreSQL to be healthy..."
	@until docker exec bilhetica-postgres pg_isready -U $(DB_USER) > /dev/null 2>&1; do \
		sleep 1; \
	done
	@echo "✅ PostgreSQL and Redis are running"
	@echo "   PostgreSQL: localhost:$(LOCAL_DB_PORT)"
	@echo "   Redis:      localhost:6379"

dev-api: ## Start the Spring Boot backend (local, requires dev-infra)
	chmod +x ./mvnw && ./mvnw spring-boot:run

dev: api-stop dev-infra ## Start infra + backend (blocking — runs API in foreground)
	@$(MAKE) dev-api

dev-stop: api-stop ## Stop local API + PostgreSQL + Redis
	docker compose down
	@echo "✅ Local infrastructure stopped"

frontend-install: ## Install frontend dependencies
	cd frontend && npm install

frontend-dev: ## Start the Vue PWA frontend development server
	cd frontend && npm run dev

# ══════════════════════════════════════════════════════════════
# DOCKER BUILD
# ══════════════════════════════════════════════════════════════

build: ## Build the Docker image (tagged :dev) — builds inside minikube's daemon when running
	@if minikube status --format='{{.Host}}' 2>/dev/null | grep -q Running; then \
		echo "🔧 Building $(IMAGE) inside minikube's Docker daemon..."; \
		eval $$(minikube docker-env) && docker build -t $(IMAGE) .; \
	else \
		docker build -t $(IMAGE) .; \
	fi
	@echo "✅ Built $(IMAGE)"

release: ## Tag a release from current git commit
	$(eval REL_TAG := $(shell git rev-parse --short HEAD))
	docker build -t $(IMAGE_NAME):$(REL_TAG) .
	docker tag $(IMAGE_NAME):$(REL_TAG) $(IMAGE_NAME):latest
	@echo "✅ Tagged $(IMAGE_NAME):$(REL_TAG) and $(IMAGE_NAME):latest"

# ══════════════════════════════════════════════════════════════
# KUBERNETES
# ══════════════════════════════════════════════════════════════

k8s-start: build ## Start Minikube, load image, deploy everything
	@echo "🚀 Starting Minikube..."
	minikube start --driver=docker --memory=$(MINIKUBE_MEM) --cpus=$(MINIKUBE_CPUS)
	@echo "📊 Enabling metrics-server (required for HPA)..."
	minikube addons enable metrics-server
	@echo "📦 Loading image into Minikube..."
	minikube image load $(IMAGE)
	@$(MAKE) k8s-deploy

k8s-deploy: ## Apply all k8s manifests (assumes Minikube is running)
	kubectl apply -f k8s/postgres.yaml
	kubectl apply -f k8s/redis.yaml
	kubectl apply -f k8s/api.yaml
	kubectl apply -f k8s/hpa.yaml
	kubectl apply -f k8s/ingress.yaml
	@echo "⏳ Waiting for pods to be ready..."
	kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s
	kubectl wait --for=condition=ready pod -l app=redis --timeout=60s
	kubectl wait --for=condition=ready pod -l app=bilhetica-api --timeout=120s
	@echo "✅ All pods running"
	@$(MAKE) k8s-status

k8s-status: ## Show pod and HPA status
	@echo ""
	@echo "── Pods ──────────────────────────────"
	@kubectl get pods -o wide
	@echo ""
	@echo "── HPA ───────────────────────────────"
	@kubectl get hpa
	@echo ""
	@echo "── Services ──────────────────────────"
	@kubectl get svc
	@echo ""

k8s-tunnel: ## Port-forward the API service to localhost (run in separate terminal)
	@echo "🔗 Forwarding API to http://127.0.0.1:$(API_PORT)"
	@echo "   Press Ctrl+C to stop"
	kubectl port-forward svc/bilhetica-api $(API_PORT):$(API_PORT)

k8s-reload: build ## Rebuild image (inside minikube's daemon) and rolling-restart the API
	kubectl rollout restart deployment/bilhetica-api
	kubectl rollout status deployment/bilhetica-api --timeout=120s
	@echo "✅ API redeployed"

k8s-logs: ## Tail API pod logs
	kubectl logs -f -l app=bilhetica-api --all-containers --max-log-requests=10

k8s-stop: ## Delete all k8s resources and stop Minikube
	-kubectl delete -f k8s/ingress.yaml 2>/dev/null
	-kubectl delete -f k8s/hpa.yaml 2>/dev/null
	-kubectl delete -f k8s/api.yaml 2>/dev/null
	-kubectl delete -f k8s/redis.yaml 2>/dev/null
	-kubectl delete -f k8s/postgres.yaml 2>/dev/null
	minikube stop
	@echo "✅ Kubernetes stopped"

# ══════════════════════════════════════════════════════════════
# DATA SEEDING
# ══════════════════════════════════════════════════════════════

pip-install: ## Create .venv and install Python dependencies
	@echo "📦 Setting up Python virtual environment..."
	@test -d .venv || python3 -m venv .venv
	.venv/bin/pip install -r requirements.txt -q
	@echo "✅ Python dependencies installed"

seed-generate: pip-install ## Generate data/seed/ CSVs + load.sql + manifest. Override: make seed-generate SEED_ROWS=1000000 NUM_UTENTES=50000 ZIPF_S=0.5
	@echo "🔄 Generating data/seed/ (SEED_ROWS=$(or $(SEED_ROWS),5000), NUM_UTENTES=$(or $(NUM_UTENTES),200), ZIPF_S=$(or $(ZIPF_S),0.8))..."
	SEED_ROWS=$(or $(SEED_ROWS),5000) NUM_UTENTES=$(or $(NUM_UTENTES),200) ZIPF_S=$(or $(ZIPF_S),0.8) PACK_FRAC=$(or $(PACK_FRAC),0.2) BILHETE_FRAC=$(or $(BILHETE_FRAC),0.2) .venv/bin/python data/converter.py
	@echo "✅ data/seed/ generated"

# load.sql uses psql \copy (client-side files). The DOCKER_PG client mounts the
# repo at /work, so the 'data/seed/*.csv' paths resolve; --network host reaches
# the compose postgres on $(LOCAL_DB_PORT).
seed-local: ## Seed the local PostgreSQL via COPY (containerized psql); run seed-generate first
	@echo "🌱 Seeding local database (COPY)..."
	$(DOCKER_PG) psql -h localhost -p $(LOCAL_DB_PORT) -U $(DB_USER) -d $(DB_NAME) -f data/seed/load.sql
	@echo "✅ Local database seeded"

seed-k8s: ## Seed the Kubernetes PostgreSQL via COPY (requires port-forward); run seed-generate first
	@echo "🌱 Starting port-forward and seeding..."
	@# Start port-forward in background, seed, then kill it
	kubectl port-forward svc/postgres $(K8S_DB_PORT):5432 &
	@PF_PID=$$!; \
	sleep 3; \
	$(DOCKER_PG) psql -h localhost -p $(K8S_DB_PORT) -U $(DB_USER) -d $(DB_NAME) -f data/seed/load.sql; \
	kill $$PF_PID 2>/dev/null; \
	echo "✅ Kubernetes database seeded"

# ── Snapshot / restore (fast repeatable resets of a built dataset) ──────────
snapshot-local: ## pg_dump the local DB to data/loadtest.dump (run after seed + indexes)
	@echo "📦 Snapshotting local database to data/loadtest.dump..."
	$(DOCKER_PG) pg_dump -h localhost -p $(LOCAL_DB_PORT) -U $(DB_USER) -Fc -d $(DB_NAME) -f data/loadtest.dump
	@echo "✅ Snapshot written to data/loadtest.dump"

restore-local: ## Restore data/loadtest.dump into the local DB (parallel)
	@echo "♻️  Restoring data/loadtest.dump into local database..."
	$(DOCKER_PG) pg_restore -h localhost -p $(LOCAL_DB_PORT) -U $(DB_USER) -j 4 --clean --if-exists -d $(DB_NAME) data/loadtest.dump
	@echo "✅ Local database restored"

snapshot-k8s: ## pg_dump the k8s DB to data/loadtest.dump (requires port-forward)
	kubectl port-forward svc/postgres $(K8S_DB_PORT):5432 &
	@PF_PID=$$!; sleep 3; \
	$(DOCKER_PG) pg_dump -h localhost -p $(K8S_DB_PORT) -U $(DB_USER) -Fc -d $(DB_NAME) -f data/loadtest.dump; \
	kill $$PF_PID 2>/dev/null; \
	echo "✅ Snapshot written to data/loadtest.dump"

restore-k8s: ## Restore data/loadtest.dump into the k8s DB (requires port-forward)
	kubectl port-forward svc/postgres $(K8S_DB_PORT):5432 &
	@PF_PID=$$!; sleep 3; \
	$(DOCKER_PG) pg_restore -h localhost -p $(K8S_DB_PORT) -U $(DB_USER) -j 4 --clean --if-exists -d $(DB_NAME) data/loadtest.dump; \
	kill $$PF_PID 2>/dev/null; \
	echo "✅ Kubernetes database restored"

# ══════════════════════════════════════════════════════════════
# DATABASE INDEXES
# ══════════════════════════════════════════════════════════════

define INDEX_SQL
CREATE INDEX IF NOT EXISTS idx_validacao_momento         ON validacao(momento);
CREATE INDEX IF NOT EXISTS idx_validacao_resultado       ON validacao(resultado);
CREATE INDEX IF NOT EXISTS idx_validacao_titulo_momento  ON validacao(titulo_id, momento);
CREATE INDEX IF NOT EXISTS idx_validacao_leitor_momento  ON validacao(leitor_id, momento);
CREATE INDEX IF NOT EXISTS idx_viagem_momento            ON viagem(momento);
CREATE INDEX IF NOT EXISTS idx_viagem_validacao          ON viagem(validacao_id);
CREATE INDEX IF NOT EXISTS idx_linha_paragem_seq         ON linha_paragem(linha_id, sentido, sequencia);
CREATE INDEX IF NOT EXISTS idx_titulo_utente             ON titulo_transporte(utente_id);
CREATE INDEX IF NOT EXISTS idx_lp_paragem                ON linha_paragem(paragem_id);
endef
export INDEX_SQL

indexes-local: ## Apply performance indexes on local PostgreSQL
	@echo "📊 Applying indexes to local database..."
	@echo "$$INDEX_SQL" | docker exec -i bilhetica-postgres psql -U $(DB_USER) -d $(DB_NAME)
	@echo "✅ Indexes applied (local)"

indexes-k8s: ## Apply performance indexes on Kubernetes PostgreSQL
	@echo "📊 Applying indexes to k8s database..."
	@kubectl exec -it $$(kubectl get pod -l app=postgres -o jsonpath="{.items[0].metadata.name}") \
		-- psql -U $(DB_USER) -d $(DB_NAME) -c "$$INDEX_SQL"
	@echo "✅ Indexes applied (k8s)"

# ══════════════════════════════════════════════════════════════
# TEST USER
# ══════════════════════════════════════════════════════════════

test-user: ## Register the default test user (maria@email.com)
	@echo "👤 Registering test user..."
	@curl -s -o /dev/null -w "HTTP %{http_code}\n" \
		-X POST http://localhost:$(API_PORT)/api/auth/registar \
		-H "Content-Type: application/json" \
		-d '{"nome":"Maria Silva","email":"maria@email.com","telemovel":"+351912345678","password":"password123"}'
	@echo "✅ Test user: maria@email.com / password123"

admin-user: ## Grant ADMIN role to maria@email.com on the local DB (run test-user first)
	@echo "🔑 Granting admin to maria@email.com..."
	@docker exec bilhetica-postgres psql -U $(DB_USER) -d $(DB_NAME) \
		-c "UPDATE utente SET admin = true WHERE email = 'maria@email.com';"
	@echo "✅ maria@email.com is now admin — log in again to get a fresh JWT"

# ══════════════════════════════════════════════════════════════
# K6 LOAD TESTS
# ══════════════════════════════════════════════════════════════

k6-validacao: ## Run the write-path validation stress test (POST /api/validacoes)
	@echo "🔥 Validation write-path stress test"
	@mkdir -p k6_results
	VUS=100 K6_REPORT_DIR=k6_results $(K6) --out csv=k6_results/validacao.csv k6/validacao-stress.js

K6 := k6 run
RESULTS_DIR := k6_results/$$(date +%Y%m%d_%H%M%S)

stress-%:
	@mkdir -p $(RESULTS_DIR)
	@echo "🔥 Running stress test with $* VUs"
	VUS=$* K6_REPORT_DIR=$(RESULTS_DIR) $(K6) --out csv=$(RESULTS_DIR)/stress_$*.csv k6/stress-test.js

stress-all: stress-100 stress-500 stress-1000 stress-1500 stress-2000
	@echo "✅ All stress tests completed"

capacity-%:
	@mkdir -p $(RESULTS_DIR)
	@echo "🔥 Running capacity test with $* RPS"
	RPS=$* K6_REPORT_DIR=$(RESULTS_DIR) $(K6) --out csv=$(RESULTS_DIR)/capacity_$*.csv k6/capacity-test.js

capacity-sweep: capacity-2000 capacity-2500 capacity-3000 capacity-3500
	@echo "✅ All capacity tests completed"

validacao-stress-%:
	@mkdir -p $(RESULTS_DIR)
	@echo "🔥 Running validation stress test with $* VUs"
	VUS=$* K6_REPORT_DIR=$(RESULTS_DIR) $(K6) --out csv=$(RESULTS_DIR)/validacao_stress_$*.csv k6/validacao-stress.js

validacao-stress-all: validacao-stress-100 validacao-stress-500 validacao-stress-1000 validacao-stress-1500 validacao-stress-2000
	@echo "✅ All validation stress tests completed"

validacao-capacity-%:
	@mkdir -p $(RESULTS_DIR)
	@echo "🔥 Running validation capacity test with $* RPS"
	RPS=$* K6_REPORT_DIR=$(RESULTS_DIR) $(K6) --out csv=$(RESULTS_DIR)/validacao_capacity_$*.csv k6/validacao-capacity.js

validacao-capacity-sweep: validacao-capacity-500 validacao-capacity-1000 validacao-capacity-1500 validacao-capacity-2000
	@echo "✅ All validation capacity tests completed"

# ══════════════════════════════════════════════════════════════
# CLEANUP
# ══════════════════════════════════════════════════════════════

clean-local: api-stop ## Stop local infra and remove volumes
	docker compose down -v
	@echo "✅ Local infrastructure and volumes removed"

clean-k8s: ## Delete k8s resources, stop Minikube, and delete cluster
	-kubectl delete -f k8s/ 2>/dev/null
	minikube delete
	@echo "✅ Minikube cluster deleted"

clean: clean-local ## Full cleanup (local + k8s if running)
	-@$(MAKE) clean-k8s 2>/dev/null
	@echo "✅ All cleaned up"