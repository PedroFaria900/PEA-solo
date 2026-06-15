package pt.uminho.mei.bilhetica.service.pagamento;

import pt.uminho.mei.bilhetica.dto.MetodoRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoPagamento;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

/**
 * Espelha TituloFactory: cada tipo de método de pagamento tem a sua
 * implementação @Component descoberta automaticamente pelo Spring.
 * O serviço recolhe-as num Map<TipoMetodoPagamento, MetodoPagamentoFactory>
 * — adicionar um tipo novo não requer editar o serviço.
 */
public interface MetodoPagamentoFactory {

    TipoMetodoPagamento tipo();

    /** Cria uma nova instância do método com os dados do pedido. */
    MetodoPagamento criar(MetodoRequest req, Utente utente);

    /**
     * Aplica as alterações do pedido ao método existente (campos tipo-específicos).
     * Chamado pelo serviço após lookup pelo tipo — sem instanceof no serviço.
     */
    void atualizar(MetodoPagamento existente, MetodoRequest req);
}
