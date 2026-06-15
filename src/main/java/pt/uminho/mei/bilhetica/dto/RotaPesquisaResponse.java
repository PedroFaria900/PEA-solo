package pt.uminho.mei.bilhetica.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotaPesquisaResponse {
    private List<LegRotaResponse> legs;
    private int tempoTotalSeg;
    /** legs.size() - 1 */
    private int numTransbordos;
    private List<String> zonasAtravessadas;
    private List<RecomendacaoTituloResponse> recomendacoes;
}
