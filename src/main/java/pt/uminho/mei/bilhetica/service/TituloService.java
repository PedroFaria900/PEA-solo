package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.service.titulo.TituloFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TituloService {

    private final TituloTransporteRepository tituloRepository;
    private final UtenteRepository utenteRepository;
    private final CarteiraService carteiraService;
    private final Map<TipoTitulo, TituloFactory> factories;

    public TituloService(TituloTransporteRepository tituloRepository,
                         UtenteRepository utenteRepository,
                         CarteiraService carteiraService,
                         List<TituloFactory> factoryList) {
        this.tituloRepository = tituloRepository;
        this.utenteRepository = utenteRepository;
        this.carteiraService = carteiraService;
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

        // saldo check + debit + registo de transação delegados ao CarteiraService
        carteiraService.debitar(utente, preco, "Compra de " + request.getTipo());

        TituloTransporte titulo = tituloRepository.save(factory.criar(request, utente));

        return toResponse(titulo);
    }

    private TituloResponse toResponse(TituloTransporte t) {
        return TituloResponse.builder()
            .id(t.getId())
            .tipo(t.tipo().name())
            .estado(t.getEstado())
            .viagensRestantes(t.viagensRestantesResponse())
            .periodo(t.periodoResponse())
            .areaGeografica(t.areaGeografica())
            .expiraEm(t.expiraEm())
            .build();
    }
}
