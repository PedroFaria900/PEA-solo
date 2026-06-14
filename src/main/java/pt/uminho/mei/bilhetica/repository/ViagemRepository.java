package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Viagem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ViagemRepository extends JpaRepository<Viagem, UUID> {

    // JOIN FETCH collapses the 3-hop lazy chain (validacao→leitor→linha) into a
    // single join query. ORDER BY is omitted here — callers pass Sort.by("momento").
    // descending() via Pageable so there is no double-ORDER-BY conflict.
    // All fetched associations are to-one, so SQL LIMIT/OFFSET applies correctly
    // with no Hibernate in-memory-pagination warning.
    @Query("""
        SELECT v FROM Viagem v
        JOIN FETCH v.validacao va
        JOIN FETCH va.leitor l
        JOIN FETCH l.linha
        WHERE va.titulo.utente.id = :utenteId
    """)
    List<Viagem> findByUtenteId(@Param("utenteId") UUID utenteId, Pageable pageable);

    @Query("""
        SELECT COUNT(v) FROM Viagem v
        WHERE v.validacao.leitor.linha.id = :linhaId
        AND v.momento >= :desde
    """)
    Long countByLinhaIdSince(
        @Param("linhaId") UUID linhaId,
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT v.validacao.leitor.linha.designacao, COUNT(v) as total
        FROM Viagem v
        WHERE v.momento >= :desde
        GROUP BY v.validacao.leitor.linha.designacao
        ORDER BY total DESC
    """)
    List<Object[]> topLinhasMaisMovimentadas(
        @Param("desde") LocalDateTime desde);
}
