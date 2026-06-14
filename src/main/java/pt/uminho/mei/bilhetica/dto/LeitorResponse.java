package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.EstadoLeitor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeitorResponse {
    private UUID id;
    private String codigo;
    private String linhaDesignacao;
    private EstadoLeitor estado;
}
