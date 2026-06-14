import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { loginPool, LEITOR_CODES, BASE_URL } from './lib/manifest.js';

/**
 * Write-path stress test — measures POST /api/validacoes latency and error rate
 * under sustained VU concurrency using a warmup + steady two-scenario shape.
 *
 * Uses PASSE títulos (unlimited, no state exhaustion — no DB reset needed).
 * Each VU uses a consistent pool slot via __VU % pool.length to guarantee
 * that concurrent VUs hit distinct título rows and avoid @Version collisions.
 */

const TARGET_VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 100;
// At minimum one pool entry per VU so each VU has its own PASSE row.
const NUM_TOKENS = __ENV.TOKENS
  ? parseInt(__ENV.TOKENS)
  : Math.max(200, TARGET_VUS);

const validacaoLatency = new Trend('validacao_latency_steady', true);

export const options = {
  setupTimeout: '10m',
  scenarios: {
    warmup: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: Math.floor(TARGET_VUS / 4) },
        { duration: '15s', target: TARGET_VUS },
      ],
      gracefulRampDown: '10s',
      exec: 'validate',
      tags: { phase: 'warmup' },
    },
    steady: {
      executor: 'constant-vus',
      vus: TARGET_VUS,
      duration: '2m',
      startTime: '30s',
      exec: 'validateSteady',
      tags: { phase: 'steady' },
    },
  },
  thresholds: {
    'http_req_duration{phase:steady,endpoint:validacao}': ['p(95)<3000'],
    'http_req_failed{phase:steady}': ['rate<0.05'],
    'checks{phase:steady}':         ['rate>0.99'],
  },
};

export function setup() {
  return { pool: loginPool(NUM_TOKENS) };
}

function doValidation(data, recordSteady) {
  const entry  = data.pool[__VU % data.pool.length];
  const leitor = LEITOR_CODES[__VU % LEITOR_CODES.length];

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

  if (recordSteady) validacaoLatency.add(res.timings.duration);

  // Think time: approximate real scanning pacing (one tap every ~0.2-0.4 s per gate)
  sleep(0.2 + Math.random() * 0.2);
}

export function validate(data)       { doValidation(data, false); }
export function validateSteady(data) { doValidation(data, true); }
