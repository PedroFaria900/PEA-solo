import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { loginPool, sampleNetworkIds, BASE_URL } from './lib/manifest.js';
import { makeHandleSummary } from './lib/summary.js';

const TARGET_VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 100;
// Scale token pool with load; each VU gets a consistent slot via __VU % pool.length
// so concurrent VUs don't compete on the same session.
const NUM_TOKENS = __ENV.TOKENS
  ? parseInt(__ENV.TOKENS)
  : Math.max(50, Math.floor(TARGET_VUS / 10));

// Custom trends per endpoint group — recorded only during the steady phase
// so warmup latency doesn't pollute the reported numbers.
const tLinhas       = new Trend('linhas_latency_steady',        true);
const tLinhaPar     = new Trend('linha_paragens_latency_steady', true);
const tParagem      = new Trend('paragem_latency_steady',       true);
const tViagens      = new Trend('viagens_latency_steady',       true);
const tTitulos      = new Trend('titulos_latency_steady',       true);
const tPublicLinhas = new Trend('public_linhas_latency_steady', true);

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
    // Enforce thresholds on the steady-state phase only
    'http_req_duration{phase:steady,endpoint:linhas}':        ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:linha_paragens}':['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:paragem}':       ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:viagens}':       ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:titulos}':       ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:public_linhas}': ['p(95)<3000'],
    'http_req_failed{phase:steady}': ['rate<0.05'],
    'checks{phase:steady}':         ['rate>0.99'],
  },
  discardResponseBodies: false,
};

export function setup() {
  const pool = loginPool(NUM_TOKENS);
  const ids  = sampleNetworkIds(pool[0].token, 20);
  return { pool, ids };
}

function doRequests(data, recordSteady) {
  const entry = data.pool[__VU % data.pool.length];
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${entry.token}`,
  };
  const { linhaIds, paragemIds } = data.ids;

  // ── 1. Public linha catalogue (network listing) ───────────────────────────
  const linhasRes = http.get(`${BASE_URL}/api/linhas`, {
    headers,
    tags: { endpoint: 'linhas' },
  });
  check(linhasRes, {
    'linhas 200':         r => r.status === 200,
    'linhas json valido': r => { try { return Array.isArray(linhasRes.json()); } catch { return false; } },
  });
  if (recordSteady) tLinhas.add(linhasRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 2. Stops for a linha (ordered stop sequence) ─────────────────────────
  const linhaId = linhaIds[__VU % linhaIds.length];
  const linhaParRes = http.get(`${BASE_URL}/api/linhas/${linhaId}/paragens`, {
    headers,
    tags: { endpoint: 'linha_paragens' },
  });
  check(linhaParRes, { 'linha_paragens 200': r => r.status === 200 });
  if (recordSteady) tLinhaPar.add(linhaParRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 3. Stop detail ────────────────────────────────────────────────────────
  const paragemId = paragemIds[__VU % paragemIds.length];
  const paragemRes = http.get(`${BASE_URL}/api/paragens/${paragemId}`, {
    headers,
    tags: { endpoint: 'paragem' },
  });
  check(paragemRes, { 'paragem 200': r => r.status === 200 });
  if (recordSteady) tParagem.add(paragemRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 4. Per-user trip history (SIC/UC2 — user journey view) ───────────────
  const viagensRes = http.get(`${BASE_URL}/api/viagens?page=0&size=20`, {
    headers,
    tags: { endpoint: 'viagens' },
  });
  check(viagensRes, {
    'viagens 200':         r => r.status === 200,
    'viagens json valido': r => { try { return Array.isArray(viagensRes.json()); } catch { return false; } },
  });
  if (recordSteady) tViagens.add(viagensRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 5. Per-user títulos (wallet view) ────────────────────────────────────
  const titulosRes = http.get(`${BASE_URL}/api/titulos`, {
    headers,
    tags: { endpoint: 'titulos' },
  });
  check(titulosRes, { 'titulos 200': r => r.status === 200 });
  if (recordSteady) tTitulos.add(titulosRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 6. Route suggestion (direct-route read — network query) ───────────────
  // Uses first two sampled stops to find routes between them
  const [pIdA, pIdB] = paragemIds;
  const rotaRes = http.get(`${BASE_URL}/api/rotas?origemId=${pIdA}&destinoId=${pIdB}`, {
    headers,
    tags: { endpoint: 'public_linhas' },
  });
  check(rotaRes, { 'rotas 200': r => r.status === 200 });
  if (recordSteady) tPublicLinhas.add(rotaRes.timings.duration);

  // End-of-iteration think time with jitter to avoid synchronized pulsing
  sleep(0.4 + Math.random() * 0.2);
}

export function browse(data)       { doRequests(data, false); }
export function browseSteady(data) { doRequests(data, true); }

export const handleSummary = makeHandleSummary('stress');
