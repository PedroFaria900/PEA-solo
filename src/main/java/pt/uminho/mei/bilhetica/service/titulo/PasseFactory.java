package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPasse;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.math.BigDecimal;

@Component
public class PasseFactory implements TituloFactory {

    private final CalculadoraTarifa calc;

    public PasseFactory(CalculadoraTarifa calc) {
        this.calc = calc;
    }

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.PASSE;
    }

    @Override
    public BigDecimal calcularPreco(ComprarTituloRequest req, Utente utente) {
        return calc.precoBase(TipoTitulo.PASSE, utente.getPerfil(), req.getZonasIds());
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        TituloPasse p = new TituloPasse();
        p.setUtente(utente);
        p.setEstado(EstadoTitulo.ATIVO);
        p.setValidade(req.getValidade());
        p.setZonas(calc.resolver(req.getZonasIds()));
        return p;
    }
}
