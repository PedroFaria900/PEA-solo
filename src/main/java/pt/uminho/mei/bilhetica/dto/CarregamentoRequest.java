package pt.uminho.mei.bilhetica.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload para carregar saldo. metodoId é opcional: se ausente usa o método
 * predefinido (se existir); se nenhum método existir ainda, o carregamento
 * é permitido na mesma (débito simulado).
 */
@Data
public class CarregamentoRequest {
    private BigDecimal valor;
    private UUID metodoId;
}
