package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.Validacao;

import java.util.UUID;

public interface ValidacaoRepository extends JpaRepository<Validacao, UUID> {
}