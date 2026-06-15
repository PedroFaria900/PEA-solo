package pt.uminho.mei.bilhetica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.uminho.mei.bilhetica.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoResponse {
    private UUID id;
    private BigDecimal valor;
    private TipoTransacao tipo;
    private LocalDateTime momento;
    private String descricao;
}
