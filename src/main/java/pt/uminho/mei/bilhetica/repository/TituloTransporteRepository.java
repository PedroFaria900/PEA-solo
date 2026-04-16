package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import java.util.List;
import java.util.UUID;

public interface TituloTransporteRepository
        extends JpaRepository<TituloTransporte, UUID> {

    List<TituloTransporte> findByUtenteId(UUID utenteId);
}