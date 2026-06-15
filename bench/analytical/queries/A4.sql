-- A4: Boardings per stop (heavy 4-table join + GROUP BY paragem).
-- This is the costliest query: no direct FK from validacao to paragem.
-- Chain: validacao → leitor → linha → linha_paragem → paragem
-- Note: a single linha can have many paragens, so the join fanout multiplies
-- the validacao rows by the number of stops on each line — counts here represent
-- "line-stop coverage" rather than per-boarding-stop (validacao captures the line,
-- not the specific stop). This is an inherent schema constraint.
-- Note: paragem.municipio is currently unindexed (no @Index on entity).
-- Potential optimization: add idx_leitor_linha on leitor(linha_id) to speed the join.
--
-- Parameters: ts_start, ts_end

SELECT
    p.id,
    p.nome,
    p.municipio,
    COUNT(v.id)::bigint AS boardings
FROM validacao v
JOIN leitor        le ON le.id        = v.leitor_id
JOIN linha          l ON l.id         = le.linha_id
JOIN linha_paragem lp ON lp.linha_id  = l.id
JOIN paragem        p ON p.id         = lp.paragem_id
WHERE v.momento   BETWEEN :'ts_start'::timestamp AND :'ts_end'::timestamp
  AND v.resultado = 'VALIDO'
GROUP BY p.id, p.nome, p.municipio
ORDER BY boardings DESC
LIMIT 50;
