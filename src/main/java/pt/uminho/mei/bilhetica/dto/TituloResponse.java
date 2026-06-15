package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.PeriodoPasse;
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
    private Integer viagensRestantes;
    /** Período do passe (MENSAL/ANUAL); null para PACK e BILHETE. */
    private PeriodoPasse periodo;
    private String areaGeografica;
    /** Instante de expiração: fim de validade (passe/pack) ou activação+1h (bilhete); null se ainda não activado. */
    private LocalDateTime expiraEm;
}
