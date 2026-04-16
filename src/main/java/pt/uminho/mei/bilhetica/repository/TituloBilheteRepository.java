package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.titulo.TituloBilhete;
import java.util.UUID;

public interface TituloBilheteRepository
        extends JpaRepository<TituloBilhete, UUID> {
}
