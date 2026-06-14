package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Validacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ValidacaoRepository extends JpaRepository<Validacao, UUID> {

    // Count validations for a given paragem by looking through the leitor's linha stops
    @Query("""
        SELECT COUNT(v) FROM Validacao v
        JOIN LinhaParagem lp ON lp.linha = v.leitor.linha
        WHERE lp.paragem.id = :paragemId
        AND v.momento >= :desde
    """)
    Long countByParagemIdSince(
        @Param("paragemId") UUID paragemId,
        @Param("desde") LocalDateTime desde);

    // Hour distribution for a paragem (via linha stops) — native query to use
    // PostgreSQL's EXTRACT(HOUR FROM ...) which has no JPQL equivalent via FUNCTION()
    @Query(value = """
        SELECT EXTRACT(HOUR FROM v.momento) AS hora, COUNT(v.id) AS total
        FROM validacao v
        JOIN leitor l ON l.id = v.leitor_id
        JOIN linha_paragem lp ON lp.linha_id = l.linha_id
        WHERE lp.paragem_id = :paragemId
        AND v.momento >= :desde
        GROUP BY EXTRACT(HOUR FROM v.momento)
        ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> countByHourForParagem(
        @Param("paragemId") UUID paragemId,
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT COUNT(v) FROM Validacao v
        WHERE v.momento >= :desde
        AND v.momento < :ate
    """)
    Long countByPeriod(
        @Param("desde") LocalDateTime desde,
        @Param("ate") LocalDateTime ate);

    @Query(value = """
        SELECT EXTRACT(HOUR FROM momento) as hora, COUNT(id) as total
        FROM validacao
        WHERE momento >= :desde
        GROUP BY EXTRACT(HOUR FROM momento)
        ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> countByHourForRede(
        @Param("desde") LocalDateTime desde);

    // Hour distribution scoped to a single linha — rides idx_validacao_leitor_momento.
    @Query(value = """
        SELECT EXTRACT(HOUR FROM v.momento) AS hora, COUNT(v.id) AS total
        FROM validacao v
        JOIN leitor l ON l.id = v.leitor_id
        WHERE l.linha_id = :linhaId
          AND v.momento >= :desde
        GROUP BY EXTRACT(HOUR FROM v.momento)
        ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> countByHourForLinha(
        @Param("linhaId") UUID linhaId,
        @Param("desde") LocalDateTime desde);
}