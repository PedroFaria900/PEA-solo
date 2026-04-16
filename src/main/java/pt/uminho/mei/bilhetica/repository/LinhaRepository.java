package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.Linha;
import pt.uminho.mei.bilhetica.enums.TipoTransporte;
import java.util.List;
import java.util.UUID;

public interface LinhaRepository extends JpaRepository<Linha, UUID> {
    List<Linha> findByTipoTransporte(TipoTransporte tipo);
}
