package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Validacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ValidacaoRepository extends JpaRepository<Validacao, UUID> {

    @Query("""
        SELECT COUNT(v) FROM Validacao v
        WHERE v.paragem.id = :paragemId
        AND v.momento >= :desde
    """)
    Long countByParagemIdSince(
        @Param("paragemId") UUID paragemId,
        @Param("desde") LocalDateTime desde);

    @Query(value = """
        SELECT EXTRACT(HOUR FROM momento) as hora, COUNT(id) as total
        FROM validacao
        WHERE paragem_id = :paragemId
        AND momento >= :desde
        GROUP BY EXTRACT(HOUR FROM momento)
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
}