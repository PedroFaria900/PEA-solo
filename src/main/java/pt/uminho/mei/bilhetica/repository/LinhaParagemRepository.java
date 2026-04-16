package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.LinhaParagem;
import pt.uminho.mei.bilhetica.entity.LinhaParagemId;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import java.util.List;

public interface LinhaParagemRepository
        extends JpaRepository<LinhaParagem, LinhaParagemId> {

    @Query("""
        SELECT lp FROM LinhaParagem lp
        WHERE lp.linha.id = :linhaId
        AND lp.id.sentido = :sentido
        ORDER BY lp.sequencia ASC
    """)
    List<LinhaParagem> findByLinhaIdAndSentidoOrdenado(
        @Param("linhaId") java.util.UUID linhaId,
        @Param("sentido") SentidoLinha sentido);

    @Query("""
        SELECT lp FROM LinhaParagem lp
        WHERE lp.paragem.id = :paragemId
    """)
    List<LinhaParagem> findByParagemId(
        @Param("paragemId") java.util.UUID paragemId);
}
