package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprarTituloRequest {
    private String tipo;
    private LocalDate validade;
    private String areaGeografica;
    private Integer viagens;
    private UUID paragemOrigemId;
    private UUID paragemDestinoId;
}
