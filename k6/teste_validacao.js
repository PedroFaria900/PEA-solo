import http from 'k6/http';
import { check, sleep } from 'k6';

// ── Configuração dos patamares de carga ──────────────────────────────────
export const options = {
  stages: [
    { duration: '30s', target: 100 },  // rampa até 100 utilizadores
    { duration: '1m',  target: 100 },  // manter 100 utilizadores
    { duration: '30s', target: 500 },  // rampa até 500 utilizadores
    { duration: '1m',  target: 500 },  // manter 500 utilizadores
    { duration: '30s', target: 1000 }, // rampa até 1000 utilizadores
    { duration: '1m',  target: 1000 }, // manter 1000 utilizadores
    { duration: '30s', target: 0 },    // descida gradual
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% dos pedidos abaixo de 2s
    http_req_failed:   ['rate<0.05'],  // menos de 5% de erros
  },
};

const BASE_URL = 'http://127.0.0.1:8080';
// ── Cenário principal ────────────────────────────────────────────────────
export default function () {

  // 1. Login
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: 'maria@email.com',
      password: 'password123'
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, {
    'login 200': (r) => r.status === 200,
  });

  if (loginRes.status !== 200) return;

  const token = loginRes.json('accessToken');
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  sleep(0.5);

  // 2. Listar títulos
  const titulosRes = http.get(`${BASE_URL}/api/titulos`, { headers });
  check(titulosRes, { 'titulos 200': (r) => r.status === 200 });

  sleep(0.5);

  // 3. Estatísticas da rede
  const redeRes = http.get(`${BASE_URL}/api/rede/estatisticas`, { headers });
  check(redeRes, { 'estatísticas 200': (r) => r.status === 200 });

  sleep(1);
}