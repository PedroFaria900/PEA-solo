package pt.uminho.mei.bilhetica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.uminho.mei.bilhetica.entity.PackTier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackTierRepository extends JpaRepository<PackTier, UUID> {

    boolean existsByViagens(int viagens);

    Optional<PackTier> findByViagens(int viagens);

    List<PackTier> findAllByOrderByViagensAsc();
}
