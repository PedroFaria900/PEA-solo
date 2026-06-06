package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Viagem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ViagemRepository extends JpaRepository<Viagem, UUID> {

    @Query("""
        SELECT v FROM Viagem v
        WHERE v.validacao.titulo.utente.id = :utenteId
        ORDER BY v.momento DESC
    """)
    List<Viagem> findByUtenteId(@Param("utenteId") UUID utenteId);

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
