package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.*;
import pt.uminho.mei.bilhetica.entity.leitor.*;
import pt.uminho.mei.bilhetica.entity.titulo.*;
import pt.uminho.mei.bilhetica.enums.*;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.service.validacao.ContextoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ValidacaoService {

    private final ValidacaoRepository validacaoRepository;
    private final ViagemRepository viagemRepository;
    private final LeitorRepository leitorRepository;
    private final TituloTransporteRepository tituloRepository;
    private final List<RegraValidacao> regras;

    public ValidacaoService(ValidacaoRepository validacaoRepository,
                            ViagemRepository viagemRepository,
                            LeitorRepository leitorRepository,
                            TituloTransporteRepository tituloRepository,
                            List<RegraValidacao> regras) {
        this.validacaoRepository = validacaoRepository;
        this.viagemRepository = viagemRepository;
        this.leitorRepository = leitorRepository;
        this.tituloRepository = tituloRepository;
        this.regras = regras; // injectadas por ordem de @Order
    }

    @Transactional
    public ValidacaoResponse processar(ValidacaoRequest request, String email) {

        // 1. Carregar título
        TituloTransporte titulo = tituloRepository.findById(request.getTituloId())
            .orElseThrow(() -> new RuntimeException("Título não encontrado"));

        // 2. Resolver o leitor a partir do código lido no QR do veículo
        Leitor leitor = leitorRepository.findByCodigo(request.getLeitorCodigo()).orElse(null);
        if (leitor == null) {
            // Sem leitor não há FK para registar a validação — resposta não persistida.
            return ValidacaoResponse.builder()
                .resultado(ResultadoValidacao.INVALIDO)
                .mensagem("Leitor desconhecido")
                .build();
        }
        if (leitor.getEstado() != EstadoLeitor.ACTIVO) {
            return criarValidacao(titulo, leitor,
                ResultadoValidacao.INVALIDO, "Leitor inactivo", null);
        }

        // 3. Avaliar a cadeia de regras; a primeira que falhar interrompe.
        LocalDateTime agora = LocalDateTime.now();
        ContextoValidacao ctx = new ContextoValidacao(titulo, leitor, agora, email);
        for (RegraValidacao regra : regras) {
            Optional<ResultadoRegra> falha = regra.verificar(ctx);
            if (falha.isPresent()) {
                return criarValidacao(titulo, leitor,
                    falha.get().resultado(), falha.get().mensagem(), null);
            }
        }

        // 4. Aplicar o efeito da validação no título (consumo/activação).
        titulo.registarConsumo(agora);
        tituloRepository.save(titulo);

        // 5. Registar validação e viagem.
        Validacao validacao = gravarValidacao(titulo, leitor, ResultadoValidacao.VALIDO);

        Viagem viagem = Viagem.builder()
            .validacao(validacao)
            .momento(agora)
            .build();
        Viagem viagemSalva = viagemRepository.save(viagem);

        return ValidacaoResponse.builder()
            .validacaoId(validacao.getId())
            .resultado(ResultadoValidacao.VALIDO)
            .mensagem("Boa viagem!")
            .viagemId(viagemSalva.getId())
            .build();
    }

    private ValidacaoResponse criarValidacao(TituloTransporte titulo,
                                             Leitor leitor,
                                             ResultadoValidacao resultado,
                                             String mensagem,
                                             java.util.UUID viagemId) {
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
