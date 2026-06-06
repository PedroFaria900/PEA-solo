package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprarTituloRequest {
    private String tipo;
    private LocalDate validade;
    private UUID zonaId; // Used for Passe and Pack
    private List<UUID> zonasIds; // Used for Bilhete
    private Integer viagens;
}
