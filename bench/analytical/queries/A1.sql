-- A1: Top-N lines by successful boardings in the time window.
-- Join chain: validacao → leitor → linha
-- Index candidates: idx_validacao_leitor_momento (leitor_id, momento)
-- Baseline planner choice: likely hash join + seq scan or index scan on validacao.
-- After optimization: covering index on (resultado, momento) INCLUDE (leitor_id) may allow
-- index-only scan, eliminating the heap fetch for the majority of filtered rows.
--
-- Parameters (psql -v): ts_start, ts_end  (timestamps matching the actual data range)

SELECT
    l.id,
    l.designacao,
    l.tipo_transporte,
    COUNT(*)::bigint AS boardings
FROM validacao v
JOIN leitor le ON le.id    = v.leitor_id
JOIN linha  l  ON l.id     = le.linha_id
WHERE v.momento   BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp
  AND v.resultado = 'VALIDO'
GROUP BY l.id, l.designacao, l.tipo_transporte
ORDER BY boardings DESC
LIMIT 20;
