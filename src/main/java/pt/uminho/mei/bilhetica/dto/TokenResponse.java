package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String token;
    private LocalDateTime expiraEm;
    private String qrCodeBase64;
}
