package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPack;
import java.util.UUID;

public interface TituloPackRepository
        extends JpaRepository<TituloPack, UUID> {
}
