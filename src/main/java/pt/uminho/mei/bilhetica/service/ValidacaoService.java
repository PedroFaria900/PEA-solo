package pt.uminho.mei.bilhetica.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.*;
import pt.uminho.mei.bilhetica.entity.leitor.*;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.*;
import pt.uminho.mei.bilhetica.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ValidacaoService {

    private final ValidacaoRepository validacaoRepository;
    private final ViagemRepository viagemRepository;
    private final LeitorRepository leitorRepository;
    private final TituloTransporteRepository tituloRepository;
    private final ZonaTarifariaRepository zonaTarifariaRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public ValidacaoService(ValidacaoRepository validacaoRepository,
                            ViagemRepository viagemRepository,
                            LeitorRepository leitorRepository,
                            TituloTransporteRepository tituloRepository,
                            ZonaTarifariaRepository zonaTarifariaRepository,
                            RedisTemplate<String, String> redisTemplate) {
        this.validacaoRepository = validacaoRepository;
        this.viagemRepository = viagemRepository;
        this.leitorRepository = leitorRepository;
        this.tituloRepository = tituloRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public ValidacaoResponse processar(ValidacaoRequest request, String email) {

        // 1. Carregar título e verificar ownership
        TituloTransporte titulo = tituloRepository.findById(request.getTituloId())
            .orElseThrow(() -> new RuntimeException("Título não encontrado"));

        if (!titulo.getUtente().getEmail().equals(email)) {
            return ValidacaoResponse.builder()
                .resultado(ResultadoValidacao.INVALIDO)
                .mensagem("Não autorizado a usar este título")
                .build();
        }

        Leitor leitor = leitorRepository.findById(request.getLeitorId())
            .orElseThrow(() -> new RuntimeException("Leitor não encontrado"));

        // 3. Verificar estado do título
        if (titulo.getEstado() != EstadoTitulo.ATIVO) {
            return criarValidacao(titulo, leitor,
                ResultadoValidacao.INVALIDO, "Título não está activo", null);
        }

        // 4. Verificar validade
        if (!verificarValidade(titulo)) {
            return criarValidacao(titulo, leitor,
                ResultadoValidacao.INVALIDO, "Título expirado", null);
        }

        // 5. Verificar saldo (PACK)
        if (titulo instanceof TituloPack pack) {
            if (pack.getViagensRestantes() <= 0) {
                return criarValidacao(titulo, leitor,
                    ResultadoValidacao.SEM_SALDO, "Pack sem viagens disponíveis", null);
            }
        }

        // 6. Verificar zona geográfica
        if (!verificarZona(titulo, leitor)) {
            return criarValidacao(titulo, leitor,
                ResultadoValidacao.FORA_ZONA, "Linha fora da área do título", null);
        }

        // 7. Decrementar pack
        if (titulo instanceof TituloPack pack) {
            pack.setViagensRestantes(pack.getViagensRestantes() - 1);
            if (pack.getViagensRestantes() == 0) {
                pack.setEstado(EstadoTitulo.ESGOTADO);
            }
            tituloRepository.save(pack);
        }

        // 8. Criar validação e viagem
        Validacao validacao = gravarValidacao(titulo, leitor, ResultadoValidacao.VALIDO);

        Viagem viagem = Viagem.builder()
            .validacao(validacao)
            .momento(LocalDateTime.now())
            .build();
        Viagem viagemSalva = viagemRepository.save(viagem);



        return ValidacaoResponse.builder()
            .validacaoId(validacao.getId())
            .resultado(ResultadoValidacao.VALIDO)
            .mensagem("Boa viagem!")
            .viagemId(viagemSalva.getId())
            .build();
    }

    private boolean verificarValidade(TituloTransporte titulo) {
        LocalDate hoje = LocalDate.now();
        LocalDate validade = null;

        if (titulo instanceof TituloPasse p) validade = p.getValidade();
        else if (titulo instanceof TituloPack p) validade = p.getValidade();
        else if (titulo instanceof TituloBilhete b) validade = b.getValidade();

        if (validade != null && validade.isBefore(hoje)) {
            titulo.setEstado(EstadoTitulo.EXPIRADO);
            tituloRepository.save(titulo);
            return false;
        }
        return true;
    }

    private boolean verificarZona(TituloTransporte titulo, Leitor leitor) {
        Set<ZonaTarifaria> zonas = new HashSet<>();

        if (titulo instanceof TituloPasse p && p.getZona() != null) {
            zonas.add(p.getZona());
        } else if (titulo instanceof TituloPack p && p.getZona() != null) {
            zonas.add(p.getZona());
        } else if (titulo instanceof TituloBilhete b && b.getZonas() != null) {
            zonas.addAll(b.getZonas());
        }

        // No zone constraint means pass-all (e.g. general pass)
        if (zonas.isEmpty()) return true;

        UUID linhaId = leitor.getLinha().getId();
        for (ZonaTarifaria zona : zonas) {
            if (zonaTarifariaRepository.isLinhaInZona(zona.getId(), linhaId)) {
                return true;
            }
        }
        return false;
    }

    private ValidacaoResponse criarValidacao(TituloTransporte titulo,
                                              Leitor leitor,
                                              ResultadoValidacao resultado,
                                              String mensagem,
                                              UUID viagemId) {
        Validacao v = gravarValidacao(titulo, leitor, resultado);
        return ValidacaoResponse.builder()
            .validacaoId(v.getId())
            .resultado(resultado)
            .mensagem(mensagem)
            .viagemId(viagemId)
            .build();
    }

    private Validacao gravarValidacao(TituloTransporte titulo,
                                       Leitor leitor,
                                       ResultadoValidacao resultado) {
        Validacao v = Validacao.builder()
            .titulo(titulo)
            .leitor(leitor)
            .momento(LocalDateTime.now())
            .resultado(resultado)
            .build();
        return validacaoRepository.save(v);
    }
}
