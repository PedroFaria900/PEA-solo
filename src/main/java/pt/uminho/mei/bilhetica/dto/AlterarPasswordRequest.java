package pt.uminho.mei.bilhetica.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlterarPasswordRequest {
    private String passwordAtual;
    private String passwordNova;
}
