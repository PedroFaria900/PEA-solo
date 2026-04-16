package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TituloResponse {
    private UUID id;
    private String tipo;
    private EstadoTitulo estado;
    private LocalDate validade;
    private Integer viagensRestantes;
    private String areaGeografica;
    private LocalDateTime tokenExpiraEm;
}