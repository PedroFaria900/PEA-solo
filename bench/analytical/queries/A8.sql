-- A8: Total validation count for a period (widest-scan BRIN target).
-- The simplest aggregate — COUNT(*) with a time range filter.
-- This is the most direct test of BRIN vs btree vs seq scan on validacao.momento:
--   - Seq scan: reads all pages regardless of filter
--   - Btree idx_validacao_momento: precise range seek, but larger index size
--   - BRIN (new, 128-page blocks): a few kilobytes vs megabytes for btree,
--     works well when heap insertion order correlates with momento (append-only pattern).
-- After partitioning: partition pruning reduces even the seq scan to target partitions.
--
-- Parameters: ts_start, ts_end

SELECT COUNT(*)::bigint AS total_validacoes
FROM validacao
WHERE momento BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp;
