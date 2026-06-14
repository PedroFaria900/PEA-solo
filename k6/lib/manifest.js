import http from 'k6/http';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8080';

/**
 * Manifest of loginable seeded users.
 * CSV columns: email,password,titulo_id_passe,titulo_id_pack,titulo_id_bilhete,zona,leitor_codigo_exemplo
 * All users share password "password". titulo_id_pack / titulo_id_bilhete are empty
 * for the ~80% of users that weren't seeded with those title types.
 */
const MANIFEST = new SharedArray('manifest', function () {
  const lines = open('../../data/seed/manifest.csv').split('\n');
  // Skip header line; filter trailing empty lines.
  return lines.slice(1).filter(l => l.trim()).map(line => {
    const c = line.split(',');
    return {
      email:         c[0],
      password:      c[1],
      tituloPasse:   c[2],
      tituloPack:    c[3],     // may be empty string
      tituloBilhete: c[4],    // may be empty string
      zona:          c[5],
      leitorExemplo: c[6],
    };
  });
});

/**
 * All active leitor codes from the seeded dataset.
 * CSV columns (no header): id,codigo,linha_id,estado
 * Use to spread writes across distinct readers instead of hammering the
 * single manifest example code.
 */
const LEITOR_CODES = new SharedArray('leitores', function () {
  return open('../../data/seed/leitor.csv')
    .split('\n')
    .filter(l => l.trim())
    .map(l => l.split(',')[1]);  // codigo column
});

/**
 * Log in the first `n` rows of the manifest in batches of `chunkSize`.
 * Returns [{email, token, tituloPasse}].
 * Throws if fewer than 95% of logins succeed.
 *
 * This replaces the old provisionTokens() approach which registered throwaway
 * users at the broken /api/auth/registar path. Seeded users log in directly.
 */
export function loginPool(n, chunkSize = 50) {
  const size = Math.min(n, MANIFEST.length);
  const pool = [];

  for (let offset = 0; offset < size; offset += chunkSize) {
    const chunk = Math.min(chunkSize, size - offset);
    const reqs = Array.from({ length: chunk }, (_, i) => {
      const u = MANIFEST[offset + i];
      return {
        method: 'POST',
        url: `${BASE_URL}/api/auth/login`,
        body: JSON.stringify({ email: u.email, password: u.password }),
        params: { headers: { 'Content-Type': 'application/json' } },
      };
    });

    const responses = http.batch(reqs);
    for (let i = 0; i < responses.length; i++) {
      const r = responses[i];
      if (r.status === 200) {
        const token = r.json('accessToken');
        if (token) {
          pool.push({
            email:       MANIFEST[offset + i].email,
            token,
            tituloPasse: MANIFEST[offset + i].tituloPasse,
          });
        }
      }
    }
  }

  console.log(`loginPool: ${pool.length}/${size} logins OK`);
  if (pool.length < size * 0.95) {
    throw new Error(`Setup failed: only ${pool.length}/${size} tokens obtained`);
  }
  return pool;
}

/**
 * Fetch a sample of valid linha IDs and paragem IDs from the public /api/linhas
 * endpoint. Call once in setup() and pass the result to default/exec functions.
 *
 * token — any valid Bearer token (used for the paragem sub-fetches, which also
 *          tolerate an auth header even though /api/linhas/** is public).
 * k     — how many linhas to keep; paragem IDs are collected from the first 5.
 */
export function sampleNetworkIds(token, k = 20) {
  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  };

  const linhasRes = http.get(`${BASE_URL}/api/linhas`, { headers });
  if (linhasRes.status !== 200) {
    throw new Error(`GET /api/linhas failed: ${linhasRes.status}`);
  }
  const linhas = linhasRes.json();
  const linhaIds = linhas.slice(0, k).map(l => l.id);

  const paragemIds = [];
  for (let i = 0; i < Math.min(5, linhaIds.length); i++) {
    const pRes = http.get(`${BASE_URL}/api/linhas/${linhaIds[i]}/paragens`, { headers });
    if (pRes.status === 200) {
      pRes.json().forEach(p => paragemIds.push(p.paragemId));
    }
  }

  return { linhaIds, paragemIds };
}

export { MANIFEST, LEITOR_CODES, BASE_URL };
