package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoResponse {
    private UUID id;
    private TipoMetodoPagamento tipo;
    private boolean predefinido;
    /** Representação mascarada (ex.: "Visa ****1234", "MBWay 912345678"). */
    private String resumo;
}
