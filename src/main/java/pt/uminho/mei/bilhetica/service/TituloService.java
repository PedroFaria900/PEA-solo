package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.Transacao;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTransacao;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.service.titulo.TituloFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TituloService {

    private final TituloTransporteRepository tituloRepository;
    private final UtenteRepository utenteRepository;
    private final TransacaoRepository transacaoRepository;
    private final Map<TipoTitulo, TituloFactory> factories;

    public TituloService(TituloTransporteRepository tituloRepository,
                         UtenteRepository utenteRepository,
                         TransacaoRepository transacaoRepository,
                         List<TituloFactory> factoryList) {
        this.tituloRepository = tituloRepository;
        this.utenteRepository = utenteRepository;
        this.transacaoRepository = transacaoRepository;
        this.factories = factoryList.stream()
            .collect(Collectors.toMap(TituloFactory::tipo, Function.identity()));
    }

    public List<TituloResponse> listarTitulos(String email) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        return tituloRepository.findByUtenteId(utente.getId())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public TituloResponse detalhe(UUID id, String email) {
        TituloTransporte titulo = tituloRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Título não encontrado"));
        if (!titulo.getUtente().getEmail().equals(email)) {
            throw new RuntimeException("Título não encontrado");
        }
        return toResponse(titulo);
    }

    @Transactional
    public TituloResponse comprar(String email, ComprarTituloRequest request) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        if (request.getTipo() == null) {
            throw new RuntimeException("Tipo de título inválido");
        }
        TituloFactory factory = factories.get(request.getTipo());
        if (factory == null) {
            throw new RuntimeException("Tipo de título inválido");
        }

        BigDecimal preco = factory.calcularPreco(request, utente);

        if (utente.getSaldo().compareTo(preco) < 0) {
            throw new RuntimeException("Saldo insuficiente. Necessário: " + preco
                + ", disponível: " + utente.getSaldo());
        }

        utente.setSaldo(utente.getSaldo().subtract(preco));
        utenteRepository.save(utente);

        TituloTransporte titulo = tituloRepository.save(factory.criar(request, utente));

        transacaoRepository.save(Transacao.builder()
            .utente(utente)
            .valor(preco.negate())
            .tipo(TipoTransacao.COMPRA)
            .momento(LocalDateTime.now())
            .descricao("Compra de " + request.getTipo())
            .build());

        return toResponse(titulo);
    }

    private TituloResponse toResponse(TituloTransporte t) {
        return TituloResponse.builder()
            .id(t.getId())
            .tipo(t.tipo().name())
            .estado(t.getEstado())
            .viagensRestantes(t.viagensRestantesResponse())
            .areaGeografica(t.areaGeografica())
            .expiraEm(t.expiraEm())
            .build();
    }
}
