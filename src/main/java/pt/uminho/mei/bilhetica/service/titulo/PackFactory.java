package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPack;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.service.PackTierService;

import java.math.BigDecimal;

@Component
public class PackFactory implements TituloFactory {

    private final CalculadoraTarifa calc;
    private final PackTierService packTierService;

    public PackFactory(CalculadoraTarifa calc, PackTierService packTierService) {
        this.calc = calc;
        this.packTierService = packTierService;
    }

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.PACK;
    }

    @Override
    public BigDecimal calcularPreco(ComprarTituloRequest req, Utente utente) {
        if (req.getViagens() == null || req.getViagens() <= 0) {
            throw new RuntimeException("Deve especificar o número de viagens do pack");
        }
        if (!packTierService.listar().contains(req.getViagens())) {
            throw new RuntimeException(
                "Número de viagens inválido. Tiers disponíveis: " + packTierService.listar());
        }
        return calc.precoBase(TipoTitulo.PACK, utente.getPerfil(), req.getZonasIds(), null)
            .multiply(BigDecimal.valueOf(req.getViagens()));
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        TituloPack p = new TituloPack();
        p.setUtente(utente);
        p.setEstado(EstadoTitulo.ATIVO);
        p.setValidade(req.getValidade());
        p.setViagensRestantes(req.getViagens());
        p.setZonas(calc.resolver(req.getZonasIds()));
        return p;
    }
}
