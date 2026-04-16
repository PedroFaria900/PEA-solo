package pt.uminho.mei.bilhetica.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.Paragem;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.security.QrCodeUtil;
import pt.uminho.mei.bilhetica.repository.TituloTransporteRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TituloService {

    private final TituloTransporteRepository tituloRepository;
    private final TituloPasseRepository passeRepository;
    private final TituloPackRepository packRepository;
    private final TituloBilheteRepository bilheteRepository;
    private final UtenteRepository utenteRepository;
    private final ParagemRepository paragemRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final QrCodeUtil qrCodeUtil;

    public TituloService(TituloTransporteRepository tituloRepository,
                         TituloPasseRepository passeRepository,
                         TituloPackRepository packRepository,
                         TituloBilheteRepository bilheteRepository,
                         UtenteRepository utenteRepository,
                         ParagemRepository paragemRepository,
                         RedisTemplate<String, String> redisTemplate,
                         QrCodeUtil qrCodeUtil) {
        this.tituloRepository = tituloRepository;
        this.passeRepository = passeRepository;
        this.packRepository = packRepository;
        this.bilheteRepository = bilheteRepository;
        this.utenteRepository = utenteRepository;
        this.paragemRepository = paragemRepository;
        this.redisTemplate = redisTemplate;
        this.qrCodeUtil = qrCodeUtil;
    }

    public List<TituloResponse> listarTitulos(String email) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        return tituloRepository.findByUtenteId(utente.getId())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public TituloResponse detalhe(UUID id) {
        return toResponse(tituloRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Título não encontrado")));
    }

    public TituloResponse comprar(String email,
                                   ComprarTituloRequest request) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        TituloTransporte titulo = switch (request.getTipo().toUpperCase()) {
            case "PASSE" -> {
                TituloPasse p = new TituloPasse();
                p.setUtente(utente);
                p.setEstado(EstadoTitulo.ATIVO);
                p.setValidade(request.getValidade());
                p.setAreaGeografica(request.getAreaGeografica());
                yield passeRepository.save(p);
            }
            case "PACK" -> {
                TituloPack p = new TituloPack();
                p.setUtente(utente);
                p.setEstado(EstadoTitulo.ATIVO);
                p.setValidade(request.getValidade());
                p.setViagensRestantes(request.getViagens());
                p.setAreaGeografica(request.getAreaGeografica());
                yield packRepository.save(p);
            }
            case "BILHETE" -> {
                Paragem origem = paragemRepository
                    .findById(request.getParagemOrigemId())
                    .orElseThrow(() -> new RuntimeException(
                        "Paragem de origem não encontrada"));
                Paragem destino = paragemRepository
                    .findById(request.getParagemDestinoId())
                    .orElseThrow(() -> new RuntimeException(
                        "Paragem de destino não encontrada"));
                TituloBilhete b = new TituloBilhete();
                b.setUtente(utente);
                b.setEstado(EstadoTitulo.ATIVO);
                b.setValidade(request.getValidade());
                b.setParagemOrigem(origem);
                b.setParagemDestino(destino);
                yield bilheteRepository.save(b);
            }
            default -> throw new RuntimeException("Tipo de título inválido");
        };

        return toResponse(titulo);
    }

    public TokenResponse gerarToken(String email, UUID tituloId) {
        TituloTransporte titulo = tituloRepository.findById(tituloId)
            .orElseThrow(() -> new RuntimeException("Título não encontrado"));

        if (!titulo.getUtente().getEmail().equals(email)) {
            throw new RuntimeException("Título não pertence ao utente");
        }

        if (titulo.getEstado() != EstadoTitulo.ATIVO) {
            throw new RuntimeException("Título não está activo");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(5);

        redisTemplate.opsForValue().set(
            "token:" + token,
            tituloId.toString(),
            Duration.ofMinutes(5));

        titulo.setTokenAtivo(token);
        titulo.setTokenExpiraEm(expiraEm);
        tituloRepository.save(titulo);

        String qrBase64 = qrCodeUtil.gerarQrBase64(token);

        return TokenResponse.builder()
            .token(token)
            .expiraEm(expiraEm)
            .qrCodeBase64(qrBase64)
            .build();
    }

    private TituloResponse toResponse(TituloTransporte t) {
        String tipo = t.getClass().getSimpleName()
            .replace("Titulo", "").toUpperCase();

        Integer viagensRestantes = null;
        String area = null;
        java.time.LocalDate validade = null;

        if (t instanceof TituloPack p) {
            viagensRestantes = p.getViagensRestantes();
            area = p.getAreaGeografica();
            validade = p.getValidade();
        } else if (t instanceof TituloPasse p) {
            area = p.getAreaGeografica();
            validade = p.getValidade();
        } else if (t instanceof TituloBilhete b) {
            validade = b.getValidade();
        }

        return TituloResponse.builder()
            .id(t.getId())
            .tipo(tipo)
            .estado(t.getEstado())
            .validade(validade)
            .viagensRestantes(viagensRestantes)
            .areaGeografica(area)
            .tokenExpiraEm(t.getTokenExpiraEm())
            .build();
    }
}
