package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViagemResponse {
    private UUID id;
    private LocalDateTime momento;
    private String linha;
}
