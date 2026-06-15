"""
Gerador de dados para a base de dados Bilhética (seed para testes de carga).

Em vez de um único `seed.sql` de INSERTs (lento de gerar e de carregar), este
script escreve um ficheiro CSV por tabela em `data/seed/` em ordem de
dependência de FK, mais um `load.sql` que os carrega via `\\copy` (ordens de
magnitude mais rápido). Emite ainda `manifest.csv` — utilizadores que fazem
login e já possuem títulos — para permitir testes de carga em endpoints de
escrita (POST /api/validacoes), que os testes só-leitura actuais não cobrem.

Decisões de modelação (ver plano):
  - O dataset está anonimizado: `Card_Number` é o índice da linha (sem
    identidade de passageiro), por isso os utentes são SINTÉTICOS.
  - As viagens são atribuídas aos utentes por uma distribuição ENVIESADA
    (lei de potência / Zipf) → poucos utentes intensivos, cauda longa; mais
    realista para as queries analíticas (UC3) sem custo de escrita.
  - Cada utente possui um PASSE passe-tudo (zonasIds vazio → validações ilimitadas
    em toda a rede, sem estado histórico a reconciliar); uma fracção
    possui também PACK/BILHETE para cobrir os caminhos de escrita.
  - As validações/viagens HISTÓRICAS ligam-se apenas aos PASSES.

Knobs (env):
  SEED_ROWS        nº de viagens históricas a amostrar do CSV  (default 5000)
  NUM_UTENTES      tamanho do pool sintético de utentes        (default 200)
  ZIPF_S           expoente do enviesamento viagem→utente      (default 0.8)
  PACK_FRAC        fracção de utentes com PACK adicional        (default 0.2)
  BILHETE_FRAC     fracção de utentes com BILHETE adicional     (default 0.2)
  CHUNK_ROWS       tamanho do chunk de leitura do CSV grande    (default 500000)
"""

import pandas as pd
import numpy as np
import pickle
import uuid
import random
import csv
import os
from datetime import datetime

# ── CONFIG ────────────────────────────────────────────────────────────────
SEED_ROWS = int(os.environ.get('SEED_ROWS', 5_000))
NUM_UTENTES = int(os.environ.get('NUM_UTENTES', 200))
ZIPF_S = float(os.environ.get('ZIPF_S', 0.8))
PACK_FRAC = float(os.environ.get('PACK_FRAC', 0.2))
BILHETE_FRAC = float(os.environ.get('BILHETE_FRAC', 0.2))
CHUNK_ROWS = int(os.environ.get('CHUNK_ROWS', 500_000))

# Hash bcrypt ($2a$, 10 rounds) verificado contra o BCryptPasswordEncoder da app:
# todos os utentes do seed partilham este hash; o plaintext é "password".
PASSWORD_HASH = '$2a$10$d2kkjGeSC3DrteXw5K3BWOzk/7zPeJCpsFEFS6ksjrEhDn/2T2K12'
PASSWORD_PLAIN = 'password'

OUT_DIR = 'data/seed'
os.makedirs(OUT_DIR, exist_ok=True)

random.seed(42)
np.random.seed(42)

print(f"Config: SEED_ROWS={SEED_ROWS}, NUM_UTENTES={NUM_UTENTES}, "
      f"ZIPF_S={ZIPF_S}, PACK_FRAC={PACK_FRAC}, BILHETE_FRAC={BILHETE_FRAC}")

# Tabelas em ordem de dependência (pais → filhos). Usada no TRUNCATE e no \copy.
TABLES = [
    'paragem', 'linha', 'linha_paragem', 'zona_tarifaria',
    'zona_tarifaria_paragem', 'tarifario', 'pack_tier', 'leitor', 'utente',
    'titulo_transporte', 'titulo_zona', 'transacao',
    'validacao', 'viagem',
]

# Lista explícita de colunas por tabela (alinhada com as entidades JPA).
COLUMNS = {
    'paragem': ['id', 'nome', 'codigo', 'municipio'],
    'linha': ['id', 'designacao', 'tipo_transporte'],
    'linha_paragem': ['linha_id', 'paragem_id', 'sentido', 'sequencia', 'tempo_estimado_seg'],
    'zona_tarifaria': ['id', 'nome', 'descricao'],
    'zona_tarifaria_paragem': ['zona_id', 'paragem_id'],
    'tarifario': ['id', 'tipo_titulo', 'perfil_utente', 'zona_id', 'periodo', 'preco'],
    'pack_tier': ['id', 'viagens'],
    'leitor': ['id', 'codigo', 'linha_id', 'estado'],
    'utente': ['id', 'nome', 'email', 'telemovel', 'password_hash', 'saldo', 'perfil', 'admin', 'version'],
    'titulo_transporte': ['id', 'utente_id', 'estado', 'tipo_titulo', 'version',
                          'validade', 'viagens_restantes', 'ativado_em', 'periodo'],
    'titulo_zona': ['titulo_id', 'zona_id'],
    'transacao': ['id', 'utente_id', 'valor', 'tipo', 'momento', 'descricao'],
    'validacao': ['id', 'titulo_id', 'leitor_id', 'momento', 'resultado'],
    'viagem': ['id', 'validacao_id', 'momento'],
}


def open_writers():
    """Abre um csv.writer por tabela. None é escrito como campo vazio → NULL no COPY CSV."""
    handles, writers = {}, {}
    for t in TABLES:
        f = open(os.path.join(OUT_DIR, f'{t}.csv'), 'w', newline='', encoding='utf-8')
        handles[t] = f
        writers[t] = csv.writer(f, lineterminator='\n')
    return handles, writers


# ── LOAD SOURCE DATA ────────────────────────────────────────────────────────
print("A carregar ficheiros...")
stops_df = pd.read_csv('data/BusStopList.csv')

routes = {}
try:
    with open('data/BusRoutes.pickle', 'rb') as f:
        routes = pickle.load(f)
except FileNotFoundError:
    print("Aviso: data/BusRoutes.pickle não encontrado. A gerar linhas fictícias.")
    routes = {'SER_DUMMY': pd.DataFrame([{'Stop_stn': s}
              for s in stops_df['BUS_STOP'].head(10).tolist()])}
print("Ficheiros carregados.")

handles, w = open_writers()

# ── PARAGENS ──────────────────────────────────────────────────────────────
stop_id_map = {}  # BUS_STOP -> uuid
for _, row in stops_df.iterrows():
    sid = str(uuid.uuid4())
    stop_id_map[row['BUS_STOP']] = sid
    w['paragem'].writerow([sid, row['BUS_STOP'], row['BUS_STOP'], 'CityX'])
print(f"Paragens: {len(stop_id_map)}")

# ── LINHAS E LINHA_PARAGEM ──────────────────────────────────────────────────
route_id_map = {}  # SER_xxx -> uuid
for route_code, df in routes.items():
    rid = str(uuid.uuid4())
    route_id_map[route_code] = rid
    w['linha'].writerow([rid, route_code, 'AUTOCARRO'])
    seq, seen = 1, set()
    for _, r in df.iterrows():
        stop = r['Stop_stn']
        if stop not in stop_id_map or stop in seen:
            continue
        seen.add(stop)
        tempo = int(r.get('sub').total_seconds()) if hasattr(r.get('sub'), 'total_seconds') else 0
        w['linha_paragem'].writerow([rid, stop_id_map[stop], 'IDA', seq, tempo])
        seq += 1
print(f"Linhas: {len(route_id_map)}")

# ── ZONAS TARIFARIAS ────────────────────────────────────────────────────────
# Apenas duas zonas geográficas. Cobertura total (passe-tudo) = zonasIds vazio;
# o tarifário global (zona IS NULL) cobre esse caso sem necessitar de zona própria.
zona_a_id = str(uuid.uuid4())
zona_b_id = str(uuid.uuid4())
w['zona_tarifaria'].writerow([zona_a_id, 'Zona A', 'Centro da cidade'])
w['zona_tarifaria'].writerow([zona_b_id, 'Zona B', 'Periferia'])
for pid in stop_id_map.values():
    w['zona_tarifaria_paragem'].writerow([zona_a_id if random.random() < 0.5 else zona_b_id, pid])

# ── TARIFARIO ───────────────────────────────────────────────────────────────
# Linha global (zona IS NULL): zonasIds=[] → "Rede completa" (passe-tudo, toda a rede).
# Linhas por zona: compras interzona somam as linhas de cada zona pedida.
# Tuplos: (tipo, perfil, zona, periodo, preco)
# PASSE tem periodo MENSAL e ANUAL (linhas separadas); PACK e BILHETE têm periodo=None.
# Anual ≈ mensal × 10 (2 meses grátis como desconto de fidelização).
tarifas = [
    # --- PASSE MENSAL ---
    ('PASSE', 'NORMAL',    None,      'MENSAL', 40.00),   # global / Rede completa
    ('PASSE', 'ESTUDANTE', None,      'MENSAL', 20.00),
    ('PASSE', 'SENIOR',    None,      'MENSAL', 20.00),
    ('PASSE', 'NORMAL',    zona_a_id, 'MENSAL', 25.00),   # zona A
    ('PASSE', 'ESTUDANTE', zona_a_id, 'MENSAL', 13.00),
    ('PASSE', 'SENIOR',    zona_a_id, 'MENSAL', 13.00),
    ('PASSE', 'NORMAL',    zona_b_id, 'MENSAL', 25.00),   # zona B
    ('PASSE', 'ESTUDANTE', zona_b_id, 'MENSAL', 13.00),
    ('PASSE', 'SENIOR',    zona_b_id, 'MENSAL', 13.00),
    # --- PASSE ANUAL (×10 = 2 meses grátis) ---
    ('PASSE', 'NORMAL',    None,      'ANUAL', 400.00),
    ('PASSE', 'ESTUDANTE', None,      'ANUAL', 200.00),
    ('PASSE', 'SENIOR',    None,      'ANUAL', 200.00),
    ('PASSE', 'NORMAL',    zona_a_id, 'ANUAL', 250.00),
    ('PASSE', 'ESTUDANTE', zona_a_id, 'ANUAL', 130.00),
    ('PASSE', 'SENIOR',    zona_a_id, 'ANUAL', 130.00),
    ('PASSE', 'NORMAL',    zona_b_id, 'ANUAL', 250.00),
    ('PASSE', 'ESTUDANTE', zona_b_id, 'ANUAL', 130.00),
    ('PASSE', 'SENIOR',    zona_b_id, 'ANUAL', 130.00),
    # --- PACK (preço por viagem; sem período) ---
    ('PACK', 'NORMAL',    None,      None, 1.30),          # global / Rede completa
    ('PACK', 'ESTUDANTE', None,      None, 1.00),
    ('PACK', 'SENIOR',    None,      None, 1.00),
    ('PACK', 'NORMAL',    zona_a_id, None, 0.90),          # zona A
    ('PACK', 'ESTUDANTE', zona_a_id, None, 0.70),
    ('PACK', 'SENIOR',    zona_a_id, None, 0.70),
    ('PACK', 'NORMAL',    zona_b_id, None, 0.90),          # zona B
    ('PACK', 'ESTUDANTE', zona_b_id, None, 0.70),
    ('PACK', 'SENIOR',    zona_b_id, None, 0.70),
    # --- BILHETE (sem período) ---
    ('BILHETE', 'NORMAL',    None,      None, 1.50),       # global / Rede completa
    ('BILHETE', 'ESTUDANTE', None,      None, 1.20),
    ('BILHETE', 'SENIOR',    None,      None, 1.20),
    ('BILHETE', 'NORMAL',    zona_a_id, None, 1.00),       # zona A
    ('BILHETE', 'ESTUDANTE', zona_a_id, None, 0.85),
    ('BILHETE', 'SENIOR',    zona_a_id, None, 0.85),
    ('BILHETE', 'NORMAL',    zona_b_id, None, 1.00),       # zona B
    ('BILHETE', 'ESTUDANTE', zona_b_id, None, 0.85),
    ('BILHETE', 'SENIOR',    zona_b_id, None, 0.85),
]
for tipo, perfil, zona, periodo, preco in tarifas:
    w['tarifario'].writerow([str(uuid.uuid4()), tipo, perfil, zona, periodo, preco])

# ── PACK TIERS ──────────────────────────────────────────────────────────────────
# Quantidades de viagens disponíveis para compra de packs (geridas por admin via API).
for v in [5, 10, 20]:
    w['pack_tier'].writerow([str(uuid.uuid4()), v])

# ── LEITORES (2-4 por linha) ─────────────────────────────────────────────────
leitor_map = {}        # route_code -> [leitor_id, ...]
all_leitor_ids = []    # plano (para fallback)
leitor_codigo_by_id = {}
for route_code, rid in route_id_map.items():
    leitor_map[route_code] = []
    for i in range(random.randint(2, 4)):
        lid = str(uuid.uuid4())
        codigo = f"{route_code}-BUS{i+1:02d}"
        w['leitor'].writerow([lid, codigo, rid, 'ACTIVO'])
        leitor_map[route_code].append(lid)
        all_leitor_ids.append(lid)
        leitor_codigo_by_id[lid] = codigo
example_leitor_codigo = leitor_codigo_by_id[all_leitor_ids[0]] if all_leitor_ids else ''

# ── UTENTES + TITULOS + TRANSACOES + MANIFEST ─────────────────────────────────
# Cada utente: login determinístico + PASSE passe-tudo (sem zona → toda a rede); fracção com PACK/BILHETE.
manifest_f = open(os.path.join(OUT_DIR, 'manifest.csv'), 'w', newline='', encoding='utf-8')
manifest = csv.writer(manifest_f, lineterminator='\n')
manifest.writerow(['email', 'password', 'titulo_id_passe', 'titulo_id_pack',
                   'titulo_id_bilhete', 'zona', 'leitor_codigo_exemplo'])

passe_tid_arr = []  # índice de utente -> uuid do PASSE (usado nas validações históricas)
for i in range(NUM_UTENTES):
    uid = str(uuid.uuid4())
    email = f"loadtest_{i}@bilhetica.com"
    perfil = random.choices(['NORMAL', 'ESTUDANTE', 'SENIOR'], weights=[80, 15, 5])[0]
    saldo = 50.00
    w['utente'].writerow([uid, f"LoadTest {i}", email, f"+3519{10000000 + i:08d}",
                          PASSWORD_HASH, saldo, perfil, False, 0])
    # Carregamento inicial coerente com o saldo.
    w['transacao'].writerow([str(uuid.uuid4()), uid, saldo, 'CARREGAMENTO',
                             '2025-01-01 00:00:00', 'Carregamento inicial'])

    # PASSE passe-tudo (sem zona → toda a rede, ATIVO, validade longínqua, período MENSAL para relatório).
    passe_id = str(uuid.uuid4())
    passe_tid_arr.append(passe_id)
    w['titulo_transporte'].writerow([passe_id, uid, 'ATIVO', 'PASSE', 0,
                                     '2030-12-31', None, None, 'MENSAL'])
    # Sem linha em titulo_zona → zonasAbrangidas() = {} → passe-tudo.

    pack_id = ''
    if random.random() < PACK_FRAC:
        pack_id = str(uuid.uuid4())
        w['titulo_transporte'].writerow([pack_id, uid, 'ATIVO', 'PACK', 0,
                                         '2030-12-31', 10, None, None])
        # Pack sem zona → passe-tudo (titulo_zona não recebe linha)

    bilhete_id = ''
    if random.random() < BILHETE_FRAC:
        bilhete_id = str(uuid.uuid4())
        w['titulo_transporte'].writerow([bilhete_id, uid, 'ATIVO', 'BILHETE', 0,
                                         None, None, None, None])
        # Sem linha em titulo_zona → passe-tudo (mesma lógica do PASSE).

    manifest.writerow([email, PASSWORD_PLAIN, passe_id, pack_id, bilhete_id,
                       'Rede completa', example_leitor_codigo])
manifest_f.close()
passe_tid_arr = np.array(passe_tid_arr, dtype=object)
print(f"Utentes: {NUM_UTENTES} (+ títulos + manifest)")

# Pesos enviesados (lei de potência) para atribuir viagens a utentes.
ranks = np.arange(1, NUM_UTENTES + 1)
weights = 1.0 / np.power(ranks, ZIPF_S)
weights /= weights.sum()


def pick_leitor(servico):
    leits = leitor_map.get(servico)
    if leits:
        return leits[random.randrange(len(leits))]
    return all_leitor_ids[random.randrange(len(all_leitor_ids))]


# ── VALIDACOES E VIAGENS (streaming, ligadas aos PASSES) ──────────────────────
valid_stops = set(stop_id_map.keys())
processed = 0
try:
    reader = pd.read_csv('data/BUS_DATA_OCT_2017.csv', nrows=SEED_ROWS, chunksize=CHUNK_ROWS)
except FileNotFoundError:
    print("Aviso: BUS_DATA_OCT_2017.csv não encontrado — sem viagens históricas.")
    reader = []

for chunk in reader:
    # Filtrar viagens cujas paragens existem na nossa lista.
    chunk = chunk[chunk['Boarding_stop_stn'].isin(valid_stops)
                  & chunk['Alighting_stop_stn'].isin(valid_stops)]
    if chunk.empty:
        continue

    momentos = pd.to_datetime(chunk['Ride_start_date'].astype(str) + ' '
                              + chunk['Ride_start_time'].astype(str), errors='coerce')
    ok = momentos.notna().to_numpy()
    n = int(ok.sum())
    if n == 0:
        continue

    user_idx = np.random.choice(NUM_UTENTES, size=n, p=weights)
    titulos = passe_tid_arr[user_idx]
    momentos_ok = momentos[ok].dt.strftime('%Y-%m-%d %H:%M:%S').to_numpy()
    servicos = chunk['Bus_Service_Number'].to_numpy()[ok]

    val_rows, via_rows = [], []
    for k in range(n):
        val_id = str(uuid.uuid4())
        viagem_id = str(uuid.uuid4())
        leitor_id = pick_leitor(servicos[k])
        val_rows.append([val_id, titulos[k], leitor_id, momentos_ok[k], 'VALIDO'])
        via_rows.append([viagem_id, val_id, momentos_ok[k]])
    w['validacao'].writerows(val_rows)
    w['viagem'].writerows(via_rows)
    processed += n
    print(f"  ... {processed} viagens processadas")

print(f"Viagens processadas: {processed}")

for f in handles.values():
    f.close()

# ── LOAD.SQL (orquestrador de \copy) ──────────────────────────────────────────
with open(os.path.join(OUT_DIR, 'load.sql'), 'w', encoding='utf-8') as f:
    f.write("-- Gerado por data/converter.py. Carrega os CSVs via \\copy (rápido).\n")
    f.write("-- Uso: psql -d bilhetica -f data/seed/load.sql\n\n")
    f.write("TRUNCATE " + ", ".join(TABLES) + " CASCADE;\n\n")
    for t in TABLES:
        cols = ", ".join(COLUMNS[t])
        f.write(f"\\copy {t} ({cols}) FROM 'data/seed/{t}.csv' WITH (FORMAT csv)\n")

print(f"\nGerado em {OUT_DIR}/: {len(TABLES)} CSVs + load.sql + manifest.csv")
print("Pronto!")
