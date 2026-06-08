package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacaoRequest {
    private UUID tituloId;
    private UUID leitorId;
}
