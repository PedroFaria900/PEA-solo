import http from 'k6/http';
import { check, sleep } from 'k6';

// Accept VUs dynamically from the environment, defaulting to 100
const TARGET_VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 100;

export const options = {
  setupTimeout: '10m', // Give setup 10 minutes to generate all tokens
  stages: [
    { duration: '30s', target: TARGET_VUS },
    { duration: '2m',  target: TARGET_VUS },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed:   ['rate<0.05'],
  },
};

const BASE_URL = 'http://127.0.0.1:8080';

export function setup() {
  const tokens = [];
  const ts = Date.now();
  // Also allow parameterizing the number of tokens to match VUs if you wish
  const NUM_TOKENS = __ENV.TOKENS ? parseInt(__ENV.TOKENS) : 50;

  for (let i = 0; i < NUM_TOKENS; i++) {
    const email = `loadtest_${ts}_${i}@bilhetica.com`;
    const registarRes = http.post(
      `${BASE_URL}/api/auth/registar`,
      JSON.stringify({
        nome: `LoadTest ${i}`,
        email: email,
        telemovel: `+35191${String(i).padStart(7,'0')}`,
        password: 'test123'
      }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    
    // Surface registration failures to the console so you know if setup is broken
    if (registarRes.status !== 200 && registarRes.status !== 201) {
       console.warn(`Failed to register user ${email}. Status: ${registarRes.status}`);
    }

    const loginRes = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email, password: 'test123' }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (loginRes.status === 200) {
      tokens.push(loginRes.json('accessToken'));
    }
  }

  console.log(`Tokens obtidos: ${tokens.length}`);
  return { tokens };
}

export default function (data) {
  const token = data.tokens[__VU % data.tokens.length];
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  const redeRes = http.get(`${BASE_URL}/api/linhas`, {
    headers,
    tags: { endpoint: 'linhas_all' }
  });
  check(redeRes, { 'linhas 200': (r) => r.status === 200 });

  //sleep(0.5);

  const linhasRes = http.get(`${BASE_URL}/api/linhas`, {
    headers,
    tags: { endpoint: 'linhas' }
  });
  check(linhasRes, { 'linhas 200': (r) => r.status === 200 });

  sleep(0.5);
}