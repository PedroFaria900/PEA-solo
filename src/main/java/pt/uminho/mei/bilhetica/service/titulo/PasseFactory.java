package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPasse;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        if (req.getPeriodo() == null) {
            throw new RuntimeException("Deve especificar o período do passe (MENSAL ou ANUAL)");
        }
        return calc.precoBase(TipoTitulo.PASSE, utente.getPerfil(), req.getZonasIds(), req.getPeriodo());
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        if (req.getPeriodo() == null) {
            throw new RuntimeException("Deve especificar o período do passe (MENSAL ou ANUAL)");
        }
        TituloPasse p = new TituloPasse();
        p.setUtente(utente);
        p.setEstado(EstadoTitulo.ATIVO);
        p.setPeriodo(req.getPeriodo());
        p.setValidade(req.getPeriodo().validadeDesde(LocalDate.now()));
        p.setZonas(calc.resolver(req.getZonasIds()));
        return p;
    }
}
