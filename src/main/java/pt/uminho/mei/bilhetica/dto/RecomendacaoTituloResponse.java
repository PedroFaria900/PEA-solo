package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecomendacaoTituloResponse {
    private TipoTitulo tipo;
    /** true quando zonasIds está vazio e o preço usa a linha global do tarifário */
    private boolean passeTudo;
    /** IDs a passar directamente em zonasIds ao comprar o título recomendado */
    private List<UUID> zonasIds;
    private List<String> zonasNomes;
    /** Preço total (PASSE / BILHETE); null para PACK */
    private BigDecimal preco;
    /** Preço por viagem (PACK); null para PASSE / BILHETE */
    private BigDecimal precoPorViagem;
    /** Tiers disponíveis com preço total por tier (PACK); null para PASSE / BILHETE */
    private List<PackTierResponse> tiers;
}
