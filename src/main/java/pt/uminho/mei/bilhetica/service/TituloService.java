package pt.uminho.mei.bilhetica.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.Paragem;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.entity.Transacao;
import pt.uminho.mei.bilhetica.entity.Tarifario;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTransacao;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.security.QrCodeUtil;
import pt.uminho.mei.bilhetica.repository.TituloTransporteRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
    private final ZonaTarifariaRepository zonaTarifariaRepository;
    private final TransacaoRepository transacaoRepository;
    private final TarifarioRepository tarifarioRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final QrCodeUtil qrCodeUtil;

    public TituloService(TituloTransporteRepository tituloRepository,
                         TituloPasseRepository passeRepository,
                         TituloPackRepository packRepository,
                         TituloBilheteRepository bilheteRepository,
                         UtenteRepository utenteRepository,
                         ParagemRepository paragemRepository,
                         ZonaTarifariaRepository zonaTarifariaRepository,
                         TransacaoRepository transacaoRepository,
                         TarifarioRepository tarifarioRepository,
                         RedisTemplate<String, String> redisTemplate,
                         QrCodeUtil qrCodeUtil) {
        this.tituloRepository = tituloRepository;
        this.passeRepository = passeRepository;
        this.packRepository = packRepository;
        this.bilheteRepository = bilheteRepository;
        this.utenteRepository = utenteRepository;
        this.paragemRepository = paragemRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
        this.transacaoRepository = transacaoRepository;
        this.tarifarioRepository = tarifarioRepository;
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

    @Transactional
    public TituloResponse comprar(String email,
                                   ComprarTituloRequest request) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        BigDecimal preco = BigDecimal.ZERO;

        if (request.getTipo().equalsIgnoreCase("BILHETE")) {
            if (request.getZonasIds() == null || request.getZonasIds().isEmpty()) {
                throw new RuntimeException("Deve especificar pelo menos uma zona para o bilhete");
            }
            for (UUID zId : request.getZonasIds()) {
                Tarifario t = tarifarioRepository.findByAtributos("BILHETE", utente.getPerfil(), zId)
                    .orElseThrow(() -> new RuntimeException("Tarifário não configurado para a zona " + zId));
                preco = preco.add(t.getPreco());
            }
        } else {
            Tarifario tarifario = tarifarioRepository.findByAtributos(
                request.getTipo().toUpperCase(), 
                utente.getPerfil(), 
                request.getZonaId()
            ).orElseThrow(() -> new RuntimeException("Tarifário não configurado para este tipo de título e perfil"));
            
            preco = tarifario.getPreco();
            if (request.getTipo().equalsIgnoreCase("PACK")) {
                preco = preco.multiply(BigDecimal.valueOf(request.getViagens()));
            }
        }

        if (utente.getSaldo().compareTo(preco) < 0) {
            throw new RuntimeException("Saldo insuficiente. Necessário: " + preco + ", disponível: " + utente.getSaldo());
        }

        utente.setSaldo(utente.getSaldo().subtract(preco));
        utenteRepository.save(utente);

        TituloTransporte titulo = switch (request.getTipo().toUpperCase()) {
            case "PASSE" -> {
                ZonaTarifaria zona = null;
                if (request.getZonaId() != null) {
                    zona = zonaTarifariaRepository.findById(request.getZonaId())
                        .orElseThrow(() -> new RuntimeException("Zona não encontrada"));
                }
                TituloPasse p = new TituloPasse();
                p.setUtente(utente);
                p.setEstado(EstadoTitulo.ATIVO);
                p.setValidade(request.getValidade());
                p.setZona(zona);
                yield passeRepository.save(p);
            }
            case "PACK" -> {
                ZonaTarifaria zona = null;
                if (request.getZonaId() != null) {
                    zona = zonaTarifariaRepository.findById(request.getZonaId())
                        .orElseThrow(() -> new RuntimeException("Zona não encontrada"));
                }
                TituloPack p = new TituloPack();
                p.setUtente(utente);
                p.setEstado(EstadoTitulo.ATIVO);
                p.setValidade(request.getValidade());
                p.setViagensRestantes(request.getViagens());
                p.setZona(zona);
                yield packRepository.save(p);
            }
            case "BILHETE" -> {
                Set<ZonaTarifaria> zonas = request.getZonasIds().stream()
                    .map(zId -> zonaTarifariaRepository.findById(zId)
                        .orElseThrow(() -> new RuntimeException("Zona não encontrada: " + zId)))
                    .collect(Collectors.toSet());
                    
                TituloBilhete b = new TituloBilhete();
                b.setUtente(utente);
                b.setEstado(EstadoTitulo.ATIVO);
                b.setValidade(request.getValidade());
                b.setZonas(zonas);
                yield bilheteRepository.save(b);
            }
            default -> throw new RuntimeException("Tipo de título inválido");
        };

        transacaoRepository.save(Transacao.builder()
            .utente(utente)
            .valor(preco.negate())
            .tipo(TipoTransacao.COMPRA)
            .momento(LocalDateTime.now())
            .descricao("Compra de " + request.getTipo())
            .build());

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
            area = p.getZona() != null ? p.getZona().getNome() : null;
            validade = p.getValidade();
        } else if (t instanceof TituloPasse p) {
            area = p.getZona() != null ? p.getZona().getNome() : null;
            validade = p.getValidade();
        } else if (t instanceof TituloBilhete b) {
            validade = b.getValidade();
            if (b.getZonas() != null && !b.getZonas().isEmpty()) {
                area = b.getZonas().stream().map(ZonaTarifaria::getNome).collect(Collectors.joining(", "));
            }
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
