import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { loginPool, sampleNetworkIds, BASE_URL } from './lib/manifest.js';

const TARGET_VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 100;
// Scale token pool with load; each VU gets a consistent slot via __VU % pool.length
// so concurrent VUs don't compete on the same session.
const NUM_TOKENS = __ENV.TOKENS
  ? parseInt(__ENV.TOKENS)
  : Math.max(50, Math.floor(TARGET_VUS / 10));

// Custom trends per endpoint group — recorded only during the steady phase
// so warmup latency doesn't pollute the reported numbers.
const tRede     = new Trend('rede_latency_steady',       true);
const tEstLinha = new Trend('est_linha_latency_steady',  true);
const tEstPar   = new Trend('est_paragem_latency_steady', true);
const tViagens  = new Trend('viagens_latency_steady',    true);
const tTitulos  = new Trend('titulos_latency_steady',    true);
const tLinhas   = new Trend('linhas_latency_steady',     true);

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
    'http_req_duration{phase:steady,endpoint:rede}':        ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:est_linha}':   ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:est_paragem}': ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:viagens}':     ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:titulos}':     ['p(95)<3000'],
    'http_req_duration{phase:steady,endpoint:linhas}':      ['p(95)<3000'],
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

  // ── 1. Network-wide aggregate (heaviest — UC3 analytics) ─────────────────
  const redeRes = http.get(`${BASE_URL}/api/admin/estatisticas/rede`, {
    headers,
    tags: { endpoint: 'rede' },
  });
  check(redeRes, {
    'rede 200':         r => r.status === 200,
    'rede json válido': r => { try { return redeRes.json() && typeof redeRes.json() === 'object'; } catch { return false; } },
  });
  if (recordSteady) tRede.add(redeRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 2. Stats by linha ─────────────────────────────────────────────────────
  const linhaId = linhaIds[__VU % linhaIds.length];
  const estLinhaRes = http.get(`${BASE_URL}/api/admin/estatisticas/linhas/${linhaId}`, {
    headers,
    tags: { endpoint: 'est_linha' },
  });
  check(estLinhaRes, { 'est_linha 200': r => r.status === 200 });
  if (recordSteady) tEstLinha.add(estLinhaRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 3. Stats by paragem ───────────────────────────────────────────────────
  const paragemId = paragemIds[__VU % paragemIds.length];
  const estParRes = http.get(`${BASE_URL}/api/admin/estatisticas/paragens/${paragemId}`, {
    headers,
    tags: { endpoint: 'est_paragem' },
  });
  check(estParRes, { 'est_paragem 200': r => r.status === 200 });
  if (recordSteady) tEstPar.add(estParRes.timings.duration);

  sleep(0.1 + Math.random() * 0.1);

  // ── 4. Per-user trip history (SIC/UC2 — user journey view) ───────────────
  const viagensRes = http.get(`${BASE_URL}/api/viagens`, {
    headers,
    tags: { endpoint: 'viagens' },
  });
  check(viagensRes, {
    'viagens 200':         r => r.status === 200,
    'viagens json válido': r => { try { return Array.isArray(viagensRes.json()); } catch { return false; } },
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

  // ── 6. Public linha catalogue (light, cache-friendly) ────────────────────
  const linhasRes = http.get(`${BASE_URL}/api/linhas`, {
    headers,
    tags: { endpoint: 'linhas' },
  });
  check(linhasRes, { 'linhas 200': r => r.status === 200 });
  if (recordSteady) tLinhas.add(linhasRes.timings.duration);

  // End-of-iteration think time with jitter to avoid synchronized pulsing
  sleep(0.4 + Math.random() * 0.2);
}

export function browse(data)       { doRequests(data, false); }
export function browseSteady(data) { doRequests(data, true); }
