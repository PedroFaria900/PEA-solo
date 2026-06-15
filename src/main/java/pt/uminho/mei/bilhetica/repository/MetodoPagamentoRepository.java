package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoPagamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, UUID> {

    List<MetodoPagamento> findByUtenteId(UUID utenteId);

    Optional<MetodoPagamento> findByUtenteIdAndPredefinidoTrue(UUID utenteId);

    long countByUtenteId(UUID utenteId);
}
