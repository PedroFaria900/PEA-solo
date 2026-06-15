package pt.uminho.mei.bilhetica.entity.pagamento;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

@Entity
@DiscriminatorValue("MBWAY")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MetodoMbway extends MetodoPagamento {

    /**
     * Número de telemóvel MBWay. Pode ser diferente de Utente.telemovel
     * (um utilizador pode associar um número de outro titular).
     */
    private String telemovel;

    @Override
    public TipoMetodoPagamento tipo() {
        return TipoMetodoPagamento.MBWAY;
    }

    @Override
    public String resumo() {
        return "MBWay " + telemovel;
    }
}
