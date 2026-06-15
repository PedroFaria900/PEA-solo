package pt.uminho.mei.bilhetica.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarPerfilRequest {
    private String nome;
    private String telemovel;
}
