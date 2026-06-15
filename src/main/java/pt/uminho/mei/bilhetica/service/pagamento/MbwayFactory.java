package pt.uminho.mei.bilhetica.service.pagamento;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.MetodoRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoMbway;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoPagamento;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

@Component
public class MbwayFactory implements MetodoPagamentoFactory {

    @Override
    public TipoMetodoPagamento tipo() {
        return TipoMetodoPagamento.MBWAY;
    }

    @Override
    public MetodoPagamento criar(MetodoRequest req, Utente utente) {
        MetodoMbway m = new MetodoMbway();
        m.setUtente(utente);
        m.setTelemovel(req.getTelemovel());
        return m;
    }

    @Override
    public void atualizar(MetodoPagamento existente, MetodoRequest req) {
        MetodoMbway m = (MetodoMbway) existente;
        if (req.getTelemovel() != null) m.setTelemovel(req.getTelemovel());
    }
}
