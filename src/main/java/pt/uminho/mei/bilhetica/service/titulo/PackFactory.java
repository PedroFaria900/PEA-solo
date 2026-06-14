package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Tarifario;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPack;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.repository.TarifarioRepository;
import pt.uminho.mei.bilhetica.repository.ZonaTarifariaRepository;

import java.math.BigDecimal;

@Component
public class PackFactory implements TituloFactory {

    private final TarifarioRepository tarifarioRepository;
    private final ZonaTarifariaRepository zonaTarifariaRepository;

    public PackFactory(TarifarioRepository tarifarioRepository,
                       ZonaTarifariaRepository zonaTarifariaRepository) {
        this.tarifarioRepository = tarifarioRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
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
        Tarifario tarifario = tarifarioRepository
            .findByAtributos(TipoTitulo.PACK, utente.getPerfil(), req.getZonaId())
            .orElseThrow(() -> new RuntimeException(
                "Tarifário não configurado para este tipo de título e perfil"));
        return tarifario.getPreco().multiply(BigDecimal.valueOf(req.getViagens()));
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        ZonaTarifaria zona = null;
        if (req.getZonaId() != null) {
            zona = zonaTarifariaRepository.findById(req.getZonaId())
                .orElseThrow(() -> new RuntimeException("Zona não encontrada"));
        }
        TituloPack p = new TituloPack();
        p.setUtente(utente);
        p.setEstado(EstadoTitulo.ATIVO);
        p.setValidade(req.getValidade());
        p.setViagensRestantes(req.getViagens());
        p.setZona(zona);
        return p;
    }
}
