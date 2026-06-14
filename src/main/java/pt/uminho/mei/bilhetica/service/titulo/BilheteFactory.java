package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Tarifario;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.entity.titulo.TituloBilhete;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.repository.TarifarioRepository;
import pt.uminho.mei.bilhetica.repository.ZonaTarifariaRepository;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BilheteFactory implements TituloFactory {

    private final TarifarioRepository tarifarioRepository;
    private final ZonaTarifariaRepository zonaTarifariaRepository;

    public BilheteFactory(TarifarioRepository tarifarioRepository,
                          ZonaTarifariaRepository zonaTarifariaRepository) {
        this.tarifarioRepository = tarifarioRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
    }

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.BILHETE;
    }

    @Override
    public BigDecimal calcularPreco(ComprarTituloRequest req, Utente utente) {
        if (req.getZonasIds() == null || req.getZonasIds().isEmpty()) {
            throw new RuntimeException("Deve especificar pelo menos uma zona para o bilhete");
        }
        BigDecimal preco = BigDecimal.ZERO;
        for (UUID zId : req.getZonasIds()) {
            Tarifario t = tarifarioRepository
                .findByAtributos(TipoTitulo.BILHETE, utente.getPerfil(), zId)
                .orElseThrow(() -> new RuntimeException(
                    "Tarifário não configurado para a zona " + zId));
            preco = preco.add(t.getPreco());
        }
        return preco;
    }

    @Override
    public TituloTransporte criar(ComprarTituloRequest req, Utente utente) {
        Set<ZonaTarifaria> zonas = req.getZonasIds().stream()
            .map(zId -> zonaTarifariaRepository.findById(zId)
                .orElseThrow(() -> new RuntimeException("Zona não encontrada: " + zId)))
            .collect(Collectors.toSet());

        TituloBilhete b = new TituloBilhete();
        b.setUtente(utente);
        b.setEstado(EstadoTitulo.ATIVO);
        b.setZonas(zonas);
        // ativadoEm fica null: o bilhete activa-se (janela de 1h) na 1ª validação.
        return b;
    }
}
