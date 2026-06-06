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

MINIKUBE_MEM  := 4096
MINIKUBE_CPUS := 2

# ── Phony targets ────────────────────────────────────────────
.PHONY: help \
        dev dev-infra dev-api dev-stop \
        build release \
        k8s-start k8s-deploy k8s-status k8s-tunnel k8s-stop \
        seed-generate seed-local seed-k8s \
        indexes-local indexes-k8s \
        test-user \
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

dev: dev-infra ## Start infra + backend (blocking — runs API in foreground)
	@$(MAKE) dev-api

dev-stop: ## Stop local PostgreSQL + Redis
	docker compose down
	@echo "✅ Local infrastructure stopped"

# ══════════════════════════════════════════════════════════════
# DOCKER BUILD
# ══════════════════════════════════════════════════════════════

build: ## Build the Docker image (tagged :dev)
	docker build -t $(IMAGE) .
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

k8s-reload: build ## Rebuild image and restart the API deployment
	minikube image load $(IMAGE)
	kubectl rollout restart deployment/bilhetica-api
	kubectl wait --for=condition=ready pod -l app=bilhetica-api --timeout=120s
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

seed-generate: ## Generate data/seed.sql from the UrbanBus dataset
	@echo "🔄 Generating seed.sql..."
	python data/converter.py
	@echo "✅ data/seed.sql generated"

seed-local: seed-generate ## Seed the local PostgreSQL (docker-compose)
	@echo "🌱 Seeding local database..."
	docker exec -i bilhetica-postgres psql -q -U $(DB_USER) -d $(DB_NAME) < data/seed.sql > /dev/null``
	@echo "✅ Local database seeded"

seed-k8s: seed-generate ## Seed the Kubernetes PostgreSQL (requires port-forward)
	@echo "🌱 Starting port-forward and seeding..."
	@# Start port-forward in background, seed, then kill it
	kubectl port-forward svc/postgres $(K8S_DB_PORT):5432 &
	@PF_PID=$$!; \
	sleep 3; \
	PGPASSWORD=$(DB_PASS) psql -h localhost -p $(K8S_DB_PORT) -U $(DB_USER) -d $(DB_NAME) -f data/seed.sql; \
	kill $$PF_PID 2>/dev/null; \
	echo "✅ Kubernetes database seeded"

# ══════════════════════════════════════════════════════════════
# DATABASE INDEXES
# ══════════════════════════════════════════════════════════════

define INDEX_SQL
CREATE INDEX IF NOT EXISTS idx_validacao_momento  ON validacao(momento);
CREATE INDEX IF NOT EXISTS idx_validacao_paragem  ON validacao(paragem_id);
CREATE INDEX IF NOT EXISTS idx_validacao_titulo   ON validacao(titulo_id);
CREATE INDEX IF NOT EXISTS idx_validacao_paragem_momento ON validacao(paragem_id, momento);
CREATE INDEX IF NOT EXISTS idx_validacao_titulo_momento  ON validacao(titulo_id, momento);
CREATE INDEX IF NOT EXISTS idx_viagem_inicio      ON viagem(inicio);
CREATE INDEX IF NOT EXISTS idx_viagem_entrada     ON viagem(val_entrada_id);
CREATE INDEX IF NOT EXISTS idx_linha_paragem_seq  ON linha_paragem(linha_id, sentido, sequencia);
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

# ══════════════════════════════════════════════════════════════
# K6 LOAD TESTS
# ══════════════════════════════════════════════════════════════

k6-validacao: ## Run the end-to-end validation load test
	@echo "🔥 Validation flow test"
	@mkdir -p k6_results
	@test -f k6/teste_validacao_e2e.js && k6 run --out csv=k6_results/validacao_e2e.csv k6/teste_validacao_e2e.js || k6 run --out csv=k6_results/validacao.csv k6/teste_validacao.js

K6 := k6 run
RESULTS_DIR := k6_results/$$(date +%Y%m%d_%H%M%S)

stress-%:
	@mkdir -p $(RESULTS_DIR)
	@echo "🔥 Running stress test with $* VUs"
	VUS=$* $(K6) --out csv=$(RESULTS_DIR)/stress_$*.csv k6/stress-test.js

stress-all: stress-100 stress-500 stress-1000 stress-1500 stress-2000
	@echo "✅ All stress tests completed"

capacity-%:
	@mkdir -p $(RESULTS_DIR)
	@echo "🔥 Running capacity test with $* RPS"
	RPS=$* $(K6) --out csv=$(RESULTS_DIR)/capacity_$*.csv k6/capacity-test.js

capacity-sweep: capacity-2000 capacity-2500 capacity-3000 capacity-3500
	@echo "✅ All capacity tests completed"

# ══════════════════════════════════════════════════════════════
# CLEANUP
# ══════════════════════════════════════════════════════════════

clean-local: ## Stop local infra and remove volumes
	docker compose down -v
	@echo "✅ Local infrastructure and volumes removed"

clean-k8s: ## Delete k8s resources, stop Minikube, and delete cluster
	-kubectl delete -f k8s/ 2>/dev/null
	minikube delete
	@echo "✅ Minikube cluster deleted"

clean: clean-local ## Full cleanup (local + k8s if running)
	-@$(MAKE) clean-k8s 2>/dev/null
	@echo "✅ All cleaned up"