-- A6: Boarding volume by (line, day) — two-key grouping matrix.
-- Heavier than A3: adds a JOIN to linha and groups on both linha + day.
-- Functional index on date_trunc('day', momento) can help if GROUP BY is the bottleneck.
-- Composite index on (leitor_id, momento) already exists (idx_validacao_leitor_momento),
-- which may be usable for the join + range filter together.
-- Partition pruning (after optimization) eliminates non-target months before the join.
--
-- Parameters: ts_start, ts_end

SELECT
    l.designacao,
    date_trunc('day', v.momento)::date AS dia,
    COUNT(*)::bigint                   AS boardings
FROM validacao v
JOIN leitor le ON le.id = v.leitor_id
JOIN linha  l  ON l.id  = le.linha_id
WHERE v.momento   BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp
  AND v.resultado = 'VALIDO'
GROUP BY l.id, l.designacao, dia
ORDER BY dia, boardings DESC;
