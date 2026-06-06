package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.Transacao;
import java.util.UUID;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {
}
