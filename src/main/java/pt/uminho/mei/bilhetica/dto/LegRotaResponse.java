package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegRotaResponse {
    private UUID linhaId;
    private String linhaDesignacao;
    private SentidoLinha sentido;
    private ParagemResponse paragemEmbarque;
    private ParagemResponse paragemSaida;
    private int tempoEstimadoSeg;
    private int numParagens;
}
