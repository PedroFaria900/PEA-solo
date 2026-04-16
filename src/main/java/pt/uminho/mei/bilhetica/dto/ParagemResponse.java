package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParagemResponse {
    private UUID id;
    private String nome;
    private String codigo;
    private String municipio;
    private Double latitude;
    private Double longitude;
}
