package pt.uminho.mei.bilhetica.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackTierResponse {
    private int viagens;
    private BigDecimal precoTotal;
}
