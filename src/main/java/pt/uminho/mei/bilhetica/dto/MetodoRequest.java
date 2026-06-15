package pt.uminho.mei.bilhetica.dto;

import lombok.Data;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

/**
 * Payload para adicionar ou editar um método de pagamento.
 * No add: tipo é obrigatório. No edit: tipo é ignorado (imutável após criação).
 * Campos cartão: ultimos4, validade, titular, marca.
 * Campo MBWay: telemovel.
 */
@Data
public class MetodoRequest {
    private TipoMetodoPagamento tipo;
    private String ultimos4;
    private String validade;
    private String titular;
    private String marca;
    private String telemovel;
}
