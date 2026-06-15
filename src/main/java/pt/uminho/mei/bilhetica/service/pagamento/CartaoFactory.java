package pt.uminho.mei.bilhetica.service.pagamento;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.MetodoRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoCartao;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoPagamento;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

@Component
public class CartaoFactory implements MetodoPagamentoFactory {

    @Override
    public TipoMetodoPagamento tipo() {
        return TipoMetodoPagamento.CARTAO;
    }

    @Override
    public MetodoPagamento criar(MetodoRequest req, Utente utente) {
        MetodoCartao m = new MetodoCartao();
        m.setUtente(utente);
        m.setUltimos4(req.getUltimos4());
        m.setValidade(req.getValidade());
        m.setTitular(req.getTitular());
        m.setMarca(req.getMarca());
        return m;
    }

    @Override
    public void atualizar(MetodoPagamento existente, MetodoRequest req) {
        // O cast é seguro: o serviço apenas invoca este método quando
        // existente.tipo() == CARTAO, garantido pelo dispatch via Map.
        MetodoCartao m = (MetodoCartao) existente;
        if (req.getUltimos4() != null) m.setUltimos4(req.getUltimos4());
        if (req.getValidade() != null) m.setValidade(req.getValidade());
        if (req.getTitular() != null) m.setTitular(req.getTitular());
        if (req.getMarca() != null) m.setMarca(req.getMarca());
    }
}
