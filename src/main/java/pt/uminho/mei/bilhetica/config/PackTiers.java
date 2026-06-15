package pt.uminho.mei.bilhetica.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tamanhos válidos de pack de viagens (bilhetica.pack.tiers em application.properties).
 * Alterar aqui muda tanto a recomendação na pesquisa de rotas como a validação na compra.
 */
@Component
@ConfigurationProperties(prefix = "bilhetica.pack")
@Data
public class PackTiers {
    private List<Integer> tiers;
}
