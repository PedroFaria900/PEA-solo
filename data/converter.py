import pandas as pd
import pickle
import uuid
import random
import os
from datetime import datetime

# ── CONFIG ────────────────────────────────────────────────────────────────
SEED_ROWS = int(os.environ.get('SEED_ROWS', 5_000))
NUM_UTENTES = int(os.environ.get('NUM_UTENTES', 200))

print(f"Config: SEED_ROWS={SEED_ROWS}, NUM_UTENTES={NUM_UTENTES}")
print("A carregar ficheiros...")

# ── LOAD DATA ─────────────────────────────────────────────────────────────
stops_df = pd.read_csv('data/BusStopList.csv')

routes = {}
try:
    with open('data/BusRoutes.pickle', 'rb') as f:
        routes = pickle.load(f)
except FileNotFoundError:
    print("Aviso: data/BusRoutes.pickle não encontrado. A gerar linhas fictícias.")
    routes = {'SER_DUMMY': pd.DataFrame([{'Stop_stn': s} for s in stops_df['BUS_STOP'].head(10).tolist()])}

try:
    trans_df = pd.read_csv('data/BUS_DATA_OCT_2017.csv', nrows=SEED_ROWS)
except FileNotFoundError:
    print("Aviso: data/BUS_DATA_OCT_2017.csv não encontrado. A gerar transações fictícias.")
    trans_df = pd.DataFrame({
        'Card_Number': ['DUMMY_CARD_1', 'DUMMY_CARD_2'],
        'Boarding_stop_stn': [stops_df['BUS_STOP'].iloc[0], stops_df['BUS_STOP'].iloc[1]],
        'Alighting_stop_stn': [stops_df['BUS_STOP'].iloc[1], stops_df['BUS_STOP'].iloc[0]],
        'Ride_start_date': ['2017-10-01', '2017-10-01'],
        'Ride_start_time': ['08:00:00', '09:00:00'],
        'Ride_end_date': ['2017-10-01', '2017-10-01'],
        'Ride_end_time': ['08:30:00', '09:30:00'],
        'Bus_Service_Number': ['SER_DUMMY', 'SER_DUMMY'],
    })
print("Ficheiros carregados.")

lines = []

# ── PARAGENS ──────────────────────────────────────────────────────────────
lines.append("-- PARAGENS")
lines.append("TRUNCATE paragem CASCADE;")
stop_id_map = {}  # BUS_STOP -> uuid

for _, row in stops_df.iterrows():
    sid = str(uuid.uuid4())
    stop_id_map[row['BUS_STOP']] = sid
    lat = round(41.45 + random.uniform(0, 0.15), 6)
    lon = round(-8.50 + random.uniform(0, 0.15), 6)
    nome = row['BUS_STOP'].replace("'", "''")
    lines.append(
        f"INSERT INTO paragem (id, nome, codigo, latitude, longitude, municipio) "
        f"VALUES ('{sid}', '{nome}', '{nome}', {lat}, {lon}, 'CityX');"
    )

print(f"Paragens: {len(stop_id_map)}")

# ── LINHAS E LINHA_PARAGEM ────────────────────────────────────────────────
lines.append("\n-- LINHAS E LINHA_PARAGEM")
lines.append("TRUNCATE linha CASCADE;")
route_id_map = {}  # SER_xxx -> uuid

for route_code, df in routes.items():
    rid = str(uuid.uuid4())
    route_id_map[route_code] = rid
    designacao = route_code.replace("'", "''")
    lines.append(
        f"INSERT INTO linha (id, designacao, tipo_transporte) "
        f"VALUES ('{rid}', '{designacao}', 'AUTOCARRO');"
    )
    seq = 1
    seen = set()
    for _, r in df.iterrows():
        stop = r['Stop_stn']
        if stop not in stop_id_map or stop in seen:
            continue
        seen.add(stop)
        pid = stop_id_map[stop]
        tempo = int(r.get('sub').total_seconds()) if hasattr(r.get('sub'), 'total_seconds') else 0
        lines.append(
            f"INSERT INTO linha_paragem (linha_id, paragem_id, sentido, sequencia, tempo_estimado_seg) "
            f"VALUES ('{rid}', '{pid}', 'IDA', {seq}, {tempo});"
        )
        seq += 1

print(f"Linhas: {len(route_id_map)}")

# ── UTENTES (synthetic pool, decoupled from card numbers) ─────────────────
lines.append("\n-- UTENTES")
lines.append("TRUNCATE utente CASCADE;")

utente_ids = []  # list of (uuid, perfil)
for i in range(NUM_UTENTES):
    uid = str(uuid.uuid4())
    email = f"utente_{i}@urbanbus.com"
    nome = f"Utente {i}"
    perfil = random.choices(['NORMAL', 'ESTUDANTE', 'SENIOR'], weights=[80, 15, 5])[0]
    utente_ids.append((uid, perfil))
    lines.append(
        f"INSERT INTO utente (id, nome, email, password_hash, saldo, perfil, version) "
        f"VALUES ('{uid}', '{nome}', '{email}', "
        f"'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LzTfztsgEMO', 0.00, '{perfil}', 0);"
    )

print(f"Utentes: {NUM_UTENTES}")

# ── ZONAS TARIFARIAS ──────────────────────────────────────────────────────
lines.append("\n-- ZONAS TARIFARIAS")
lines.append("TRUNCATE zona_tarifaria CASCADE;")
zona_rede_id = str(uuid.uuid4())
zona_a_id = str(uuid.uuid4())
zona_b_id = str(uuid.uuid4())

lines.append(f"INSERT INTO zona_tarifaria (id, nome, descricao) VALUES ('{zona_rede_id}', 'Rede Completa', 'Toda a rede de transportes');")
lines.append(f"INSERT INTO zona_tarifaria (id, nome, descricao) VALUES ('{zona_a_id}', 'Zona A', 'Centro da cidade');")
lines.append(f"INSERT INTO zona_tarifaria (id, nome, descricao) VALUES ('{zona_b_id}', 'Zona B', 'Periferia');")

for pid in stop_id_map.values():
    lines.append(f"INSERT INTO zona_tarifaria_paragem (zona_id, paragem_id) VALUES ('{zona_rede_id}', '{pid}');")
    if random.choice([True, False]):
        lines.append(f"INSERT INTO zona_tarifaria_paragem (zona_id, paragem_id) VALUES ('{zona_a_id}', '{pid}');")
    else:
        lines.append(f"INSERT INTO zona_tarifaria_paragem (zona_id, paragem_id) VALUES ('{zona_b_id}', '{pid}');")

# ── TARIFARIO ─────────────────────────────────────────────────────────────
lines.append("\n-- TARIFARIO")
lines.append("TRUNCATE tarifario CASCADE;")

# PASSE - Rede Completa
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'PASSE', 'NORMAL', '{zona_rede_id}', 40.00);")
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'PASSE', 'ESTUDANTE', '{zona_rede_id}', 20.00);")
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'PASSE', 'SENIOR', '{zona_rede_id}', 20.00);")

# BILHETE - Default/No Zone
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'BILHETE', 'NORMAL', NULL, 1.50);")
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'BILHETE', 'ESTUDANTE', NULL, 1.20);")
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'BILHETE', 'SENIOR', NULL, 1.20);")

# PACK - Default/No Zone
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'PACK', 'NORMAL', NULL, 1.30);")
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'PACK', 'ESTUDANTE', NULL, 1.00);")
lines.append(f"INSERT INTO tarifario (id, tipo_titulo, perfil_utente, zona_id, preco) VALUES ('{uuid.uuid4()}', 'PACK', 'SENIOR', NULL, 1.00);")

# ── LEITORES (2-4 per line) ──────────────────────────────────────────────
lines.append("\n-- LEITORES")
lines.append("TRUNCATE leitor CASCADE;")
leitor_map = {}  # route_code -> [leitor_id, ...]

for route_code, rid in route_id_map.items():
    num_leitores = random.randint(2, 4)
    leitor_map[route_code] = []
    for i in range(num_leitores):
        lid = str(uuid.uuid4())
        codigo = f"{route_code}-BUS{i+1:02d}"
        leitor_map[route_code].append(lid)
        lines.append(
            f"INSERT INTO leitor (id, codigo, linha_id, estado) "
            f"VALUES ('{lid}', '{codigo}', '{rid}', 'ACTIVO');"
        )

# ── TITULOS (one per user) ───────────────────────────────────────────────
lines.append("\n-- TITULOS TRANSPORTE")
lines.append("TRUNCATE titulo_transporte CASCADE;")
lines.append("TRUNCATE validacao CASCADE;")
lines.append("TRUNCATE viagem CASCADE;")

titulo_map = {}  # utente_uuid -> titulo_uuid
for uid, perfil in utente_ids:
    tid = str(uuid.uuid4())
    titulo_map[uid] = tid
    lines.append(
        f"INSERT INTO titulo_transporte (id, utente_id, estado, token_ativo, token_expira_em, tipo_titulo, validade, zona_id, version) "
        f"VALUES ('{tid}', '{uid}', 'ATIVO', NULL, NULL, 'PASSE', '2018-12-31', '{zona_rede_id}', 0);"
    )

print(f"Títulos: {len(titulo_map)}")

# ── PRE-FILTER TRIPS ─────────────────────────────────────────────────────
# Drop rows where boarding or alighting stops don't exist in our stop list
valid_stops = set(stop_id_map.keys())
before = len(trans_df)
trans_df = trans_df[
    trans_df['Boarding_stop_stn'].isin(valid_stops) &
    trans_df['Alighting_stop_stn'].isin(valid_stops)
].copy()
after = len(trans_df)
print(f"Viagens pré-filtradas: {before} -> {after} ({before - after} sem paragens válidas)")

# ── VALIDACOES E VIAGENS ─────────────────────────────────────────────────
lines.append("\n-- VALIDACOES E VIAGENS")

processed = 0
parse_errors = 0

for _, row in trans_df.iterrows():
    boarding = row['Boarding_stop_stn']
    bus_service = row.get('Bus_Service_Number', None)

    # Assign trip to a random user from our pool
    uid, _ = random.choice(utente_ids)
    tid = titulo_map[uid]

    # Pick a leitor: prefer one from the same bus service line
    if bus_service and bus_service in leitor_map:
        leitor_id = random.choice(leitor_map[bus_service])
    else:
        route_code = random.choice(list(leitor_map.keys()))
        leitor_id = random.choice(leitor_map[route_code])

    try:
        inicio = datetime.strptime(
            f"{row['Ride_start_date']} {row['Ride_start_time']}", "%Y-%m-%d %H:%M:%S")
    except Exception:
        parse_errors += 1
        continue

    val_id = str(uuid.uuid4())
    viagem_id = str(uuid.uuid4())

    # Validacao (single scan at boarding)
    lines.append(
        f"INSERT INTO validacao (id, titulo_id, leitor_id, momento, resultado) "
        f"VALUES ('{val_id}', '{tid}', '{leitor_id}', "
        f"'{inicio.strftime('%Y-%m-%d %H:%M:%S')}', 'VALIDO');"
    )

    # Viagem
    lines.append(
        f"INSERT INTO viagem (id, validacao_id, momento) "
        f"VALUES ('{viagem_id}', '{val_id}', '{inicio.strftime('%Y-%m-%d %H:%M:%S')}');"
    )

    processed += 1

print(f"Viagens processadas: {processed}, erros de parse: {parse_errors}")

# ── ESCREVER FICHEIRO ─────────────────────────────────────────────────────
with open('data/seed.sql', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print(f"\nFicheiro data/seed.sql gerado com {len(lines)} linhas SQL.")
print("Pronto!")