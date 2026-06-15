package pt.uminho.mei.bilhetica.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.config.PackTiers;
import pt.uminho.mei.bilhetica.entity.PackTier;
import pt.uminho.mei.bilhetica.repository.PackTierRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestão dos tiers de viagens disponíveis para compra de packs.
 * Os tiers são armazenados em base de dados e editáveis via API de admin.
 * Na primeira inicialização (tabela vazia) usa os valores de {@code bilhetica.pack.tiers}
 * como ponto de partida.
 */
@Service
public class PackTierService {

    private final PackTierRepository packTierRepository;
    private final PackTiers packTiersConfig;

    public PackTierService(PackTierRepository packTierRepository, PackTiers packTiersConfig) {
        this.packTierRepository = packTierRepository;
        this.packTiersConfig = packTiersConfig;
    }

    /**
     * Garante que a tabela não fica vazia em ambientes frescos (sem seed carregado).
     * A seed de carga (load.sql) já popula a tabela, por isso este método só actua
     * quando não existe nenhum registo.
     */
    @PostConstruct
    public void seedDefault() {
        if (packTierRepository.count() == 0) {
            for (int v : packTiersConfig.getTiers()) {
                packTierRepository.save(PackTier.builder().viagens(v).build());
            }
        }
    }

    /** Devolve os tiers em ordem crescente de viagens. */
    public List<Integer> listar() {
        return packTierRepository.findAllByOrderByViagensAsc()
            .stream()
            .map(PackTier::getViagens)
            .collect(Collectors.toList());
    }

    /** Acrescenta um tier. Rejeita duplicados e valores inválidos. */
    public void adicionar(int viagens) {
        if (viagens <= 0) {
            throw new RuntimeException("O número de viagens deve ser positivo");
        }
        if (packTierRepository.existsByViagens(viagens)) {
            throw new RuntimeException("Tier " + viagens + " já existe");
        }
        packTierRepository.save(PackTier.builder().viagens(viagens).build());
    }

    /** Remove um tier. Não falha em silêncio se o tier não existir. */
    public void remover(int viagens) {
        PackTier tier = packTierRepository.findByViagens(viagens)
            .orElseThrow(() -> new RuntimeException("Tier " + viagens + " não encontrado"));
        packTierRepository.delete(tier);
    }
}
