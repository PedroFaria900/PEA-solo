import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8080';

/**
 * Registers and logs in N users in chunks to avoid overwhelming
 * the auth endpoint and DB connection pool during setup.
 */
export function provisionTokens(numTokens, chunkSize = 50) {
  const ts = Date.now();
  const tokens = [];
  let registered = 0;
  let loggedIn = 0;

  for (let offset = 0; offset < numTokens; offset += chunkSize) {
    const size = Math.min(chunkSize, numTokens - offset);

    // Register chunk
    const registerReqs = Array.from({ length: size }, (_, i) => {
      const idx = offset + i;
      return {
        method: 'POST',
        url: `${BASE_URL}/api/auth/registar`,
        body: JSON.stringify({
          nome: `LoadTest ${idx}`,
          email: `loadtest_${ts}_${idx}@bilhetica.com`,
          telemovel: `+3519${String(10000000 + idx).padStart(8, '0')}`,
          password: 'test123',
        }),
        params: { headers: { 'Content-Type': 'application/json' } },
      };
    });
    const regResponses = http.batch(registerReqs);
    registered += regResponses.filter((r) => r.status === 200 || r.status === 201).length;

    // Login chunk
    const loginReqs = Array.from({ length: size }, (_, i) => {
      const idx = offset + i;
      return {
        method: 'POST',
        url: `${BASE_URL}/api/auth/login`,
        body: JSON.stringify({
          email: `loadtest_${ts}_${idx}@bilhetica.com`,
          password: 'test123',
        }),
        params: { headers: { 'Content-Type': 'application/json' } },
      };
    });
    const loginResponses = http.batch(loginReqs);

    for (const r of loginResponses) {
      if (r.status === 200) {
        const token = r.json('accessToken');
        if (token) {
          tokens.push(token);
          loggedIn++;
        }
      }
    }
  }

  console.log(`Setup: ${registered} registered, ${loggedIn} logged in, ${tokens.length} tokens`);

  if (tokens.length < numTokens * 0.95) {
    throw new Error(`Setup failed: only ${tokens.length}/${numTokens} tokens obtained`);
  }

  return tokens;
}

export { BASE_URL };
