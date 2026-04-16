package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Viagem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViagemRepository extends JpaRepository<Viagem, UUID> {

    @Query("""
        SELECT v FROM Viagem v
        WHERE v.valEntrada.titulo.id = :tituloId
        AND v.valSaida IS NULL
    """)
    Optional<Viagem> findViagemAbertaPorTitulo(
        @Param("tituloId") UUID tituloId);

    @Query("""
        SELECT v FROM Viagem v
        WHERE v.valEntrada.titulo.utente.id = :utenteId
        ORDER BY v.inicio DESC
    """)
    List<Viagem> findByUtenteId(@Param("utenteId") UUID utenteId);

    @Query("""
        SELECT COUNT(v) FROM Viagem v
        JOIN LinhaParagem lp ON lp.paragem = v.valEntrada.paragem
        WHERE lp.linha.id = :linhaId
        AND v.inicio >= :desde
    """)
    Long countByLinhaIdSince(
        @Param("linhaId") UUID linhaId,
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT v.valEntrada.paragem.nome, COUNT(v) as total
        FROM Viagem v
        JOIN LinhaParagem lp ON lp.paragem = v.valEntrada.paragem
        WHERE lp.linha.id = :linhaId
        AND v.inicio >= :desde
        GROUP BY v.valEntrada.paragem.nome
        ORDER BY total DESC
    """)
    List<Object[]> topOrigensParaLinha(
        @Param("linhaId") UUID linhaId,
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT v.valSaida.paragem.nome, COUNT(v) as total
        FROM Viagem v
        JOIN LinhaParagem lp ON lp.paragem = v.valSaida.paragem
        WHERE lp.linha.id = :linhaId
        AND v.valSaida IS NOT NULL
        AND v.inicio >= :desde
        GROUP BY v.valSaida.paragem.nome
        ORDER BY total DESC
    """)
    List<Object[]> topDestinosParaLinha(
        @Param("linhaId") UUID linhaId,
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT lp.linha.designacao, COUNT(v) as total
        FROM Viagem v
        JOIN LinhaParagem lp ON lp.paragem = v.valEntrada.paragem
        WHERE v.valEntrada.paragem.id = :paragemId
        AND v.inicio >= :desde
        GROUP BY lp.linha.designacao
        ORDER BY total DESC
    """)
    List<Object[]> topLinhasParaParagem(
        @Param("paragemId") UUID paragemId,
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT v.valEntrada.paragem.nome, COUNT(v) as total
        FROM Viagem v
        WHERE v.inicio >= :desde
        GROUP BY v.valEntrada.paragem.nome
        ORDER BY total DESC
    """)
    List<Object[]> topParagensMaisMovimentadas(
        @Param("desde") LocalDateTime desde);

    @Query("""
        SELECT lp.linha.designacao, COUNT(v) as total
        FROM Viagem v
        JOIN LinhaParagem lp ON lp.paragem = v.valEntrada.paragem
        WHERE v.inicio >= :desde
        GROUP BY lp.linha.designacao
        ORDER BY total DESC
    """)
    List<Object[]> topLinhasMaisMovimentadas(
        @Param("desde") LocalDateTime desde);
}
