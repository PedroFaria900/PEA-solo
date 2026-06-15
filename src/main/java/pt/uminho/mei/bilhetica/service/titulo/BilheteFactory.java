package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.TituloBilhete;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.math.BigDecimal;

@Component
public class BilheteFactory implements TituloFactory {

    private final CalculadoraTarifa calc;

    public BilheteFactory(CalculadoraTarifa calc) {
        this.calc = calc;
    }

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.BILHETE;
    }

    @Override
    public BigDecimal calcularPreco(ComprarTituloRequest req, Utente utente) {
        // Lista vazia → tarifário global (bilhete sem restrição de zona). Sem período (null).
        return calc.precoBase(TipoTitulo.BILHETE, utente.getPerfil(), req.getZonasIds(), null);
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        TituloBilhete b = new TituloBilhete();
        b.setUtente(utente);
        b.setEstado(EstadoTitulo.ATIVO);
        b.setZonas(calc.resolver(req.getZonasIds()));
        // ativadoEm fica null: o bilhete activa-se (janela de 1h) na 1ª validação.
        return b;
    }
}
