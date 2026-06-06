package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.uminho.mei.bilhetica.entity.Tarifario;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;
import java.util.Optional;
import java.util.UUID;

public interface TarifarioRepository extends JpaRepository<Tarifario, UUID> {
    
    @Query("SELECT t FROM Tarifario t WHERE t.tipoTitulo = :tipoTitulo AND t.perfilUtente = :perfilUtente AND (t.zona.id = :zonaId OR (t.zona IS NULL AND :zonaId IS NULL))")
    Optional<Tarifario> findByAtributos(@Param("tipoTitulo") String tipoTitulo, @Param("perfilUtente") PerfilUtente perfilUtente, @Param("zonaId") UUID zonaId);
}
