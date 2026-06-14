package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprarTituloRequest {
    private TipoTitulo tipo;
    private LocalDate validade; // Usado por Passe e Pack (Bilhete activa-se na 1ª validação)
    private UUID zonaId; // Used for Passe and Pack
    private List<UUID> zonasIds; // Used for Bilhete
    private Integer viagens;
}
