package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinhaParagemResponse {
    private UUID paragemId;
    private String paragemNome;
    private String paragemCodigo;
    private Double latitude;
    private Double longitude;
    private SentidoLinha sentido;
    private Integer sequencia;
    private Integer distanciaMetros;
    private Integer tempoEstimadoSeg;
}
