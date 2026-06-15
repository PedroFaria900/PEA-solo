-- A5: Validation result breakdown in the time window.
-- Exercises idx_validacao_resultado (GROUP BY resultado) combined with
-- idx_validacao_momento (range filter on momento).
-- Expected: bitmap AND scan on both indexes, or HashAgg on seq scan.
-- With a partial index WHERE resultado='VALIDO' the VALIDO count could be
-- an index-only count. The multi-result breakdown still needs a heap scan.
--
-- Parameters: ts_start, ts_end

SELECT
    resultado,
    COUNT(*)::bigint AS total
FROM validacao
WHERE momento BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp
GROUP BY resultado
ORDER BY total DESC;
