-- A3: Daily boarding volume time-series over the time window.
-- PRIMARY BRIN target: this is a pure range scan on momento with date_trunc GROUP BY.
-- Append-only fact tables (validacao, viagem) insert rows in roughly chronological order
-- so BRIN correlates well with physical tuple order — expected to match btree at <1% size.
-- After optimization: BRIN on momento should reduce scanned pages dramatically
-- vs seq scan (especially if data is time-ordered in heap).
-- After partitioning: partition pruning eliminates all but the target months entirely.
--
-- Parameters: ts_start, ts_end

SELECT
    date_trunc('day', momento)::date AS dia,
    COUNT(*)::bigint                 AS boardings
FROM validacao
WHERE momento BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp
  AND resultado = 'VALIDO'
GROUP BY dia
ORDER BY dia;
