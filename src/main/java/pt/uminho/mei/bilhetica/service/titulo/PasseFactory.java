package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Tarifario;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.entity.titulo.TituloPasse;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.repository.TarifarioRepository;
import pt.uminho.mei.bilhetica.repository.ZonaTarifariaRepository;

import java.math.BigDecimal;

@Component
public class PasseFactory implements TituloFactory {

    private final TarifarioRepository tarifarioRepository;
    private final ZonaTarifariaRepository zonaTarifariaRepository;

    public PasseFactory(TarifarioRepository tarifarioRepository,
                        ZonaTarifariaRepository zonaTarifariaRepository) {
        this.tarifarioRepository = tarifarioRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
    }

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.PASSE;
    }

    @Override
    public BigDecimal calcularPreco(ComprarTituloRequest req, Utente utente) {
        Tarifario tarifario = tarifarioRepository
            .findByAtributos(TipoTitulo.PASSE, utente.getPerfil(), req.getZonaId())
            .orElseThrow(() -> new RuntimeException(
                "Tarifário não configurado para este tipo de título e perfil"));
        return tarifario.getPreco();
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        ZonaTarifaria zona = null;
        if (req.getZonaId() != null) {
            zona = zonaTarifariaRepository.findById(req.getZonaId())
                .orElseThrow(() -> new RuntimeException("Zona não encontrada"));
        }
        TituloPasse p = new TituloPasse();
        p.setUtente(utente);
        p.setEstado(EstadoTitulo.ATIVO);
        p.setValidade(req.getValidade());
        p.setZona(zona);
        return p;
    }
}
