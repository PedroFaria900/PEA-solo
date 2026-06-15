package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.Transacao;

import java.util.List;
import java.util.UUID;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    // Transacao.utente is a direct FK — no JOIN FETCH chain needed.
    // Caller passes Pageable with sort=momento,DESC to get newest-first.
    List<Transacao> findByUtenteId(UUID utenteId, Pageable pageable);
}
