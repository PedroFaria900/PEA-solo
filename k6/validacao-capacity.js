import http from 'k6/http';
import { check } from 'k6';
import { loginPool, LEITOR_CODES, BASE_URL } from './lib/manifest.js';
import { makeHandleSummary } from './lib/summary.js';

/**
 * Write-path capacity test — measures maximum sustained POST /api/validacoes
 * throughput (validations/s) under an arrival-rate executor.
 *
 * Uses PASSE títulos (Rede Completa, unlimited, no state exhaustion) so:
 *  - resultado is always VALIDO for a valid leitor
 *  - no DB reset is needed between runs
 *
 * Each iteration picks a random pool entry to spread writes across distinct
 * título rows and avoid @Version optimistic-lock collisions. At NUM_TOKENS=1000
 * and ~10 ms write txn, in-flight C ≈ RPS × 0.01; collision rate ≈ C²/(2N)
 * which stays below 1% up to ~3500 RPS.
 */

const TARGET_RPS = __ENV.RPS ? parseInt(__ENV.RPS) : 500;
// Login pool large enough to stay well past the @Version contention floor.
// Override with TOKENS env var for higher RPS sweeps.
const NUM_TOKENS = __ENV.TOKENS ? parseInt(__ENV.TOKENS) : 1000;

export const options = {
  setupTimeout: '10m',
  scenarios: {
    ramp_to_target: {
      executor: 'ramping-arrival-rate',
      startRate: Math.floor(TARGET_RPS / 4),
      timeUnit: '1s',
      preAllocatedVUs: 500,
      maxVUs: 3000,
      stages: [
        { duration: '30s', target: Math.floor(TARGET_RPS / 2) }, // warmup
        { duration: '30s', target: TARGET_RPS },                 // ramp
        { duration: '3m',  target: TARGET_RPS },                 // hold
        { duration: '30s', target: 0 },                          // drain
      ],
      gracefulStop: '30s',
    },
  },
  thresholds: {
    'http_req_duration{endpoint:validacao}': ['p(95)<3000', 'p(99)<5000'],
    'http_req_failed':    ['rate<0.05'],
    'checks':             ['rate>0.99'],
    'dropped_iterations': ['count<100'],
  },
};

export function setup() {
  return { pool: loginPool(NUM_TOKENS) };
}

export const handleSummary = makeHandleSummary('validacao-capacity');

export default function (data) {
  const entry  = data.pool[Math.floor(Math.random() * data.pool.length)];
  const leitor = LEITOR_CODES[Math.floor(Math.random() * LEITOR_CODES.length)];

  const res = http.post(
    `${BASE_URL}/api/validacoes`,
    JSON.stringify({ tituloId: entry.tituloPasse, leitorCodigo: leitor }),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${entry.token}`,
      },
      tags: { endpoint: 'validacao' },
    },
  );

  check(res, {
    'validacao 200':    r => r.status === 200,
    'resultado VALIDO': r => {
      try { return r.json('resultado') === 'VALIDO'; } catch { return false; }
    },
  });
}
