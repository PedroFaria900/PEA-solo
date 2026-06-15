package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZonaTarifariaRepository extends JpaRepository<ZonaTarifaria, UUID> {

    Optional<ZonaTarifaria> findByNome(String nome);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ZonaTarifaria z
        JOIN z.paragens p
        WHERE z.id = :zonaId AND p.id = :paragemId
    """)
    boolean isParagemInZona(
        @Param("zonaId") UUID zonaId,
        @Param("paragemId") UUID paragemId);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ZonaTarifaria z
        JOIN z.paragens p
        JOIN LinhaParagem lp ON lp.paragem = p
        WHERE z.id = :zonaId AND lp.linha.id = :linhaId
    """)
    boolean isLinhaInZona(
        @Param("zonaId") UUID zonaId,
        @Param("linhaId") UUID linhaId);

    @Query("SELECT DISTINCT z FROM ZonaTarifaria z JOIN z.paragens p WHERE p.id IN :paragemIds")
    List<ZonaTarifaria> findByParagemIdsIn(@Param("paragemIds") Collection<UUID> paragemIds);
}
