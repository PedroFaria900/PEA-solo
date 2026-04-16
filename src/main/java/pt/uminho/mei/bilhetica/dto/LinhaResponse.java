package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoTransporte;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinhaResponse {
    private UUID id;
    private String designacao;
    private TipoTransporte tipoTransporte;
}
