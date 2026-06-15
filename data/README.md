# Data generation & seeding

`converter.py` turns the anonymized UrbanBus dataset into a database seed
**built for load testing** — including the *write* path (`POST /api/validacoes`),
which the read-only k6 tests can't reach today.

## Pipeline (tiered)

```
converter.py ──> data/seed/*.csv  +  load.sql  +  manifest.csv     (generate)
                      │
   psql -f load.sql (\copy, host-side) ──> populated DB            (fast first load)
                      │
   pg_dump -Fc ──> data/loadtest.dump ──> pg_restore -j            (instant resets)
```

- **COPY** (`load.sql`, per-table CSVs) is 10–100× faster than the old row-by-row
  `INSERT`s. Use it to (re)generate at any scale.
- **Snapshot** a built DB once, then `restore` it before each run for a fast,
  repeatable reset to a known corpus.

## Commands

```bash
make seed-local                                  # generate + COPY-load (local docker)
make seed-local SEED_ROWS=10000000 NUM_UTENTES=100000
make indexes-local                               # apply performance indexes
make snapshot-local                              # pg_dump -Fc -> data/loadtest.dump
make restore-local                               # pg_restore -j (fast reset)
```

Seeding runs `psql`/`pg_dump`/`pg_restore` in a **throwaway `postgres:16-alpine`
container** (`DOCKER_PG` in the Makefile) — no host psql install needed. The repo
is mounted at `/work` so `\copy`'s client-side file paths resolve, and
`--network host` reaches the compose postgres. k8s variants (`seed-k8s`,
`snapshot-k8s`, `restore-k8s`) tunnel via `kubectl port-forward` first.

## Knobs (env vars)

| Var | Default | Meaning |
|-----|---------|---------|
| `SEED_ROWS`    | 5000   | historical trips sampled from the 11 GB CSV |
| `NUM_UTENTES`  | 200    | synthetic loginable user pool |
| `ZIPF_S`       | 0.8    | trip→user skew exponent (higher = more concentrated) |
| `PACK_FRAC`    | 0.2    | fraction of users also owning a PACK |
| `BILHETE_FRAC` | 0.2    | fraction of users also owning a BILHETE |
| `CHUNK_ROWS`   | 500000 | CSV streaming chunk size (memory ↔ speed) |

### Recommended scale pairings

| Use | `NUM_UTENTES` | `SEED_ROWS` |
|-----|---------------|-------------|
| Laptop / Minikube k6 | ~10 000  | ~1 000 000  |
| Serious write/analytics stress | ~100 000 | ~10 000 000 |

Sizing rationale (write path): the validation title row carries an optimistic
`@Version` lock, so concurrent validations of the **same** title collide. Keep
the pool well above the contention floor `N ≫ C²/2`, where
`C ≈ target_write_RPS × write_txn_duration` (in-flight writes). At ~3500 RPS /
~10 ms that floor is only ~600 titles — so ~100 k users is far past it. Provision
generously (each user+PASSE row is tiny) so the **data** is never the bottleneck;
have the write test pick titles ~uniformly so the effective pool ≈ `N`.

## Data shape

- **Users are synthetic.** `Card_Number` in the dataset is just the row index
  (no rider identity), so trips are assigned to the synthetic pool via a skewed
  (Zipf) distribution — realistic per-user history for analytics, with no write
  cost (live contention depends on the test's runtime picks, not the seed).
- **Every user owns a Rede-Completa PASSE** (unlimited validations → stable
  steady-state write load, no historical state to reconcile). A fraction also
  own PACK / BILHETE for write-path coverage (decrement / activation /
  exhaustion). Historical `validacao`/`viagem` rows attach to PASSEs only.
- **`manifest.csv`** lists every loginable user:
  `email,password,titulo_id_passe,titulo_id_pack,titulo_id_bilhete,zona,leitor_codigo_exemplo`.
  Password plaintext is `password` for all seeded users. A write test reads a row,
  `POST /api/auth/login`, then `POST /api/validacoes {tituloId, leitorCodigo}`.

## Rebuilding

`data/seed/`, `*.dump`, and the big CSVs are git-ignored. To rebuild from scratch:
place `BusStopList.csv`, `BusRoutes.pickle`, and `BUS_DATA_OCT_2017.csv` in `data/`,
then `make seed-local && make indexes-local && make snapshot-local`.
