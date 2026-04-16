package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotaResponse {
    private UUID linhaId;
    private String linhaDesignacao;
    private ParagemResponse paragemEmbarque;
    private ParagemResponse paragemSaida;
    private int tempoEstimadoSeg;
    private int numerParagens;
}