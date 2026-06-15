package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilResponse {
    private UUID id;
    private String nome;
    private String email;
    private String telemovel;
    private PerfilUtente perfil;
    private BigDecimal saldo;
}
