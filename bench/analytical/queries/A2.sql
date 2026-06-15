-- A2: Boardings by hour-of-day across the full dataset (no time-window filter).
-- Wide scan — exercises idx_validacao_resultado since we filter resultado='VALIDO'.
-- The hash aggregation on extract(hour) is cheap once the scan is bounded.
-- Expected: seq scan or bitmap index scan on idx_validacao_resultado.
-- After optimization: partial index WHERE resultado='VALIDO' could narrow the scan.
-- No ts_start/ts_end parameters — intentionally a full-table analytical aggregate.

SELECT
    EXTRACT(HOUR FROM momento)::int AS hora,
    COUNT(*)::bigint                AS boardings
FROM validacao
WHERE resultado = 'VALIDO'
GROUP BY hora
ORDER BY hora;
