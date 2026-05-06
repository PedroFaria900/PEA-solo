import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 500 },
    { duration: '2m',  target: 500 },
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

  for (let i = 0; i < 50; i++) {
    const email = `loadtest_${ts}_${i}@bilhetica.com`;
    http.post(
      `${BASE_URL}/api/auth/registar`,
      JSON.stringify({
        nome: `LoadTest ${i}`,
        email: email,
        telemovel: `+35191${String(i).padStart(7,'0')}`,
        password: 'test123'
      }),
      { headers: { 'Content-Type': 'application/json' } }
    );

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

  const redeRes = http.get(`${BASE_URL}/api/rede/estatisticas`, { headers });
  check(redeRes, { 'rede 200': (r) => r.status === 200 });

  sleep(0.5);

  const linhasRes = http.get(`${BASE_URL}/api/linhas`, { headers });
  check(linhasRes, { 'linhas 200': (r) => r.status === 200 });

  sleep(1);
}