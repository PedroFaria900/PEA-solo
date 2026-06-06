import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { provisionTokens, BASE_URL } from './lib/setup.js';

const TARGET_VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 100;
// Scale token pool with load to avoid 40+ VUs sharing one token
const NUM_TOKENS = __ENV.TOKENS
  ? parseInt(__ENV.TOKENS)
  : Math.max(50, Math.floor(TARGET_VUS / 10));

// Custom trends to track iteration cost without warmup pollution
const redeLatency = new Trend('rede_latency_steady', true);
const linhasLatency = new Trend('linhas_latency_steady', true);

export const options = {
  setupTimeout: '5m',
  scenarios: {
    warmup: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: Math.floor(TARGET_VUS / 4) },
        { duration: '15s', target: TARGET_VUS },
      ],
      gracefulRampDown: '10s',
      exec: 'browse',
      tags: { phase: 'warmup' },
    },
    steady: {
      executor: 'constant-vus',
      vus: TARGET_VUS,
      duration: '2m',
      startTime: '30s',
      exec: 'browseSteady',
      tags: { phase: 'steady' },
    },
  },
  thresholds: {
    // Only enforce thresholds on the steady-state phase
    'http_req_duration{phase:steady,endpoint:rede}':   ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:linhas}': ['p(95)<3000'],
    'http_req_failed{phase:steady}': ['rate<0.05'],
    'checks{phase:steady}': ['rate>0.99'],
  },
  // Reduce memory pressure at high VU counts; we don't need response bodies after checks
  discardResponseBodies: false,
};

export function setup() {
  const tokens = provisionTokens(NUM_TOKENS);
  return { tokens };
}

function doRequests(data, recordSteady) {
  const token = data.tokens[__VU % data.tokens.length];
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  };

  const redeRes = http.get(`${BASE_URL}/api/rede/estatisticas`, {
    headers,
    tags: { endpoint: 'rede' },
  });
  check(redeRes, {
    'rede 200': (r) => r.status === 200,
    'rede json válido': (r) => {
      try {
        const j = r.json();
        return j && typeof j === 'object';
      } catch {
        return false;
      }
    },
  });
  if (recordSteady) redeLatency.add(redeRes.timings.duration);

  // Small inter-request pause: realistic frontend pacing
  sleep(0.1 + Math.random() * 0.1);

  const linhasRes = http.get(`${BASE_URL}/api/linhas`, {
    headers,
    tags: { endpoint: 'linhas' },
  });
  check(linhasRes, {
    'linhas 200': (r) => r.status === 200,
    'linhas json válido': (r) => {
      try {
        const j = r.json();
        return Array.isArray(j) || (j && typeof j === 'object');
      } catch {
        return false;
      }
    },
  });
  if (recordSteady) linhasLatency.add(linhasRes.timings.duration);

  // End-of-iteration think time with jitter to avoid synchronized pulsing
  sleep(0.4 + Math.random() * 0.2);
}

export function browse(data) {
  doRequests(data, false);
}

export function browseSteady(data) {
  doRequests(data, true);
}
