package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPasse;
import java.util.UUID;

public interface TituloPasseRepository
        extends JpaRepository<TituloPasse, UUID> {
}
