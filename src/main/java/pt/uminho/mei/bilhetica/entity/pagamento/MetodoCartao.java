package pt.uminho.mei.bilhetica.entity.pagamento;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

@Entity
@DiscriminatorValue("CARTAO")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MetodoCartao extends MetodoPagamento {

    /** Apenas os últimos 4 dígitos — nunca se armazena o PAN completo. */
    private String ultimos4;

    /** Data de validade no formato MM/YY. */
    private String validade;

    private String titular;

    /** Ex.: "Visa", "Mastercard". */
    private String marca;

    @Override
    public TipoMetodoPagamento tipo() {
        return TipoMetodoPagamento.CARTAO;
    }

    @Override
    public String resumo() {
        return marca + " ****" + ultimos4;
    }
}
