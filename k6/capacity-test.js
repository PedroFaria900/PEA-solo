import http from 'k6/http';
import { check } from 'k6';
import { loginPool, sampleNetworkIds, BASE_URL } from './lib/manifest.js';
import { makeHandleSummary } from './lib/summary.js';

const TARGET_RPS = __ENV.RPS ? parseInt(__ENV.RPS) : 2000;
const NUM_TOKENS = __ENV.TOKENS ? parseInt(__ENV.TOKENS) : 200;

export const options = {
  setupTimeout: '5m',
  scenarios: {
    ramp_to_target: {
      executor: 'ramping-arrival-rate',
      startRate: Math.floor(TARGET_RPS / 4),
      timeUnit: '1s',
      preAllocatedVUs: 500,
      maxVUs: 5000,
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
    'http_req_duration{endpoint:linhas}':         ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{endpoint:linha_paragens}': ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{endpoint:paragem}':        ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{endpoint:viagens}':        ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{endpoint:titulos}':        ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{endpoint:rotas}':          ['p(95)<3000', 'p(99)<5000'],
    'http_req_failed':    ['rate<0.05'],
    'checks':             ['rate>0.99'],
    // Alert if k6 can't keep up with the requested rate
    'dropped_iterations': ['count<100'],
  },
};

export function setup() {
  const pool = loginPool(NUM_TOKENS);
  const ids  = sampleNetworkIds(pool[0].token, 20);
  return { pool, ids };
}

export const handleSummary = makeHandleSummary('capacity');

export default function (data) {
  // Random token selection: at arrival-rate, __VU is less meaningful
  const entry = data.pool[Math.floor(Math.random() * data.pool.length)];
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${entry.token}`,
  };
  const { linhaIds, paragemIds } = data.ids;

  // Weighted endpoint mix per iteration (arrival-rate counts iterations as requests)
  const roll = Math.random();

  if (roll < 0.20) {
    // Public linha catalogue (network listing)
    const res = http.get(`${BASE_URL}/api/linhas`, {
      headers,
      tags: { endpoint: 'linhas' },
    });
    check(res, { 'linhas 200': r => r.status === 200 });

  } else if (roll < 0.35) {
    // Stops for a linha (ordered stop sequence per route)
    const id = linhaIds[Math.floor(Math.random() * linhaIds.length)];
    const res = http.get(`${BASE_URL}/api/linhas/${id}/paragens`, {
      headers,
      tags: { endpoint: 'linha_paragens' },
    });
    check(res, { 'linha_paragens 200': r => r.status === 200 });

  } else if (roll < 0.50) {
    // Stop detail
    const id = paragemIds[Math.floor(Math.random() * paragemIds.length)];
    const res = http.get(`${BASE_URL}/api/paragens/${id}`, {
      headers,
      tags: { endpoint: 'paragem' },
    });
    check(res, { 'paragem 200': r => r.status === 200 });

  } else if (roll < 0.65) {
    // Per-user trip history (SIC/UC2 user history view)
    const res = http.get(`${BASE_URL}/api/viagens?page=0&size=20`, {
      headers,
      tags: { endpoint: 'viagens' },
    });
    check(res, { 'viagens 200': r => r.status === 200 });

  } else if (roll < 0.80) {
    // Per-user títulos (user title wallet view)
    const res = http.get(`${BASE_URL}/api/titulos`, {
      headers,
      tags: { endpoint: 'titulos' },
    });
    check(res, { 'titulos 200': r => r.status === 200 });

  } else {
    // Route suggestion between two sampled stops (direct-route network query)
    const [pIdA, pIdB] = paragemIds;
    const res = http.get(`${BASE_URL}/api/rotas?origemId=${pIdA}&destinoId=${pIdB}`, {
      headers,
      tags: { endpoint: 'rotas' },
    });
    check(res, { 'rotas 200': r => r.status === 200 });
  }
}
