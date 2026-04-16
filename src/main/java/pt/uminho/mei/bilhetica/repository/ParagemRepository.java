package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Paragem;
import java.util.List;
import java.util.UUID;

public interface ParagemRepository extends JpaRepository<Paragem, UUID> {

    @Query("""
        SELECT DISTINCT p FROM Paragem p
        JOIN LinhaParagem lp ON lp.paragem = p
        WHERE lp.linha.id = :linhaId
        ORDER BY p.nome
    """)
    List<Paragem> findByLinhaId(@Param("linhaId") UUID linhaId);
}
