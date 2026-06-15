package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistarRequest {
    private String nome;
    private String email;
    private String telemovel;
    private String password;
    private PerfilUtente perfil;
}
