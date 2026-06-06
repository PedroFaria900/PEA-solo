import http from 'k6/http';
import { check } from 'k6';
import { provisionTokens, BASE_URL } from './lib/setup.js';

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
    'http_req_duration{endpoint:rede}':   ['p(95)<3000', 'p(99)<5000'],
    'http_req_duration{endpoint:linhas}': ['p(95)<3000', 'p(99)<5000'],
    'http_req_failed': ['rate<0.05'],
    'checks': ['rate>0.99'],
    // Alert if k6 can't keep up with the requested rate
    'dropped_iterations': ['count<100'],
  },
};

export function setup() {
  const tokens = provisionTokens(NUM_TOKENS);
  return { tokens };
}

export default function (data) {
  // Random token selection: at arrival-rate, __VU is less meaningful
  const token = data.tokens[Math.floor(Math.random() * data.tokens.length)];
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  };

  // Pick one endpoint per iteration (arrival-rate counts iterations as requests)
  // 50/50 split between the two endpoints
  const hitRede = Math.random() < 0.5;

  if (hitRede) {
    const res = http.get(`${BASE_URL}/api/rede/estatisticas`, {
      headers,
      tags: { endpoint: 'rede' },
    });
    check(res, { 'rede 200': (r) => r.status === 200 });
  } else {
    const res = http.get(`${BASE_URL}/api/linhas`, {
      headers,
      tags: { endpoint: 'linhas' },
    });
    check(res, { 'linhas 200': (r) => r.status === 200 });
  }
}
