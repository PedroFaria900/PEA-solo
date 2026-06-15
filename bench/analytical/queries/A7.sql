-- A7: Distinct active users per line (COUNT DISTINCT dedup).
-- Chain: validacao → titulo_transporte (for utente_id) + validacao → leitor → linha
-- COUNT(DISTINCT utente_id) forces a dedup pass per group — expensive on large data.
-- No direct optimization shortcut exists for COUNT(DISTINCT ...) with current indexes;
-- a covering index on validacao(resultado, momento) INCLUDE (leitor_id, titulo_id)
-- could allow an index-only scan, avoiding the heap for the entire query.
-- The idx_titulo_utente index helps if the planner chooses a nested loop via titulo
-- but for aggregate COUNT DISTINCT the hash approach usually wins at scale.
--
-- Parameters: ts_start, ts_end

SELECT
    l.id,
    l.designacao,
    COUNT(DISTINCT tt.utente_id)::bigint AS utentes_unicos
FROM validacao v
JOIN leitor             le ON le.id  = v.leitor_id
JOIN linha               l ON l.id   = le.linha_id
JOIN titulo_transporte  tt ON tt.id  = v.titulo_id
WHERE v.momento   BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp
  AND v.resultado = 'VALIDO'
GROUP BY l.id, l.designacao
ORDER BY utentes_unicos DESC;
