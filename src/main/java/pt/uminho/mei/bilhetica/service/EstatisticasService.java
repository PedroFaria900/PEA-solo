package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.Paragem;
import pt.uminho.mei.bilhetica.entity.Linha;
import pt.uminho.mei.bilhetica.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EstatisticasService {

    private final ValidacaoRepository validacaoRepository;
    private final ViagemRepository viagemRepository;
    private final ParagemRepository paragemRepository;
    private final LinhaRepository linhaRepository;

    public EstatisticasService(ValidacaoRepository validacaoRepository,
                                ViagemRepository viagemRepository,
                                ParagemRepository paragemRepository,
                                LinhaRepository linhaRepository) {
        this.validacaoRepository = validacaoRepository;
        this.viagemRepository = viagemRepository;
        this.paragemRepository = paragemRepository;
        this.linhaRepository = linhaRepository;
    }

    public EstatisticasParagemResponse estatisticasParagem(UUID paragemId) {
        Paragem paragem = paragemRepository.findById(paragemId)
            .orElseThrow(() -> new RuntimeException("Paragem não encontrada"));

        LocalDateTime desde24h = LocalDateTime.now().minusHours(24);

        Long total24h = validacaoRepository
            .countByParagemIdSince(paragemId, desde24h);

        List<Object[]> porHora = validacaoRepository
            .countByHourForParagem(paragemId, desde24h);

        Integer horaPico = porHora.isEmpty() ? null
            : ((Number) porHora.get(0)[0]).intValue();

        double media = porHora.isEmpty() ? 0
            : porHora.stream()
                .mapToLong(r -> ((Number) r[1]).longValue())
                .average().orElse(0);

        List<Object[]> topLinhasRaw = viagemRepository
            .topLinhasParaParagem(paragemId, desde24h);

        List<EstatisticasParagemResponse.LinhaAfluenciaDTO> topLinhas =
            topLinhasRaw.stream()
                .limit(3)
                .map(r -> EstatisticasParagemResponse.LinhaAfluenciaDTO.builder()
                    .linhaDesignacao((String) r[0])
                    .totalViagens(((Number) r[1]).longValue())
                    .build())
                .collect(Collectors.toList());

        return EstatisticasParagemResponse.builder()
            .paragemNome(paragem.getNome())
            .totalValidacoes24h(total24h)
            .horaPico(horaPico)
            .mediaValidacoesPorHora(Math.round(media * 10.0) / 10.0)
            .topLinhas(topLinhas)
            .build();
    }

    public EstatisticasLinhaResponse estatisticasLinha(UUID linhaId) {
        Linha linha = linhaRepository.findById(linhaId)
            .orElseThrow(() -> new RuntimeException("Linha não encontrada"));

        LocalDateTime desde = LocalDateTime.now().minusDays(30);

        Long totalViagens = viagemRepository
            .countByLinhaIdSince(linhaId, desde);

        List<Object[]> origens = viagemRepository
            .topOrigensParaLinha(linhaId, desde);
        String origemMaisFrequente = origens.isEmpty()
            ? null : (String) origens.get(0)[0];

        List<Object[]> destinos = viagemRepository
            .topDestinosParaLinha(linhaId, desde);
        String destinoMaisFrequente = destinos.isEmpty()
            ? null : (String) destinos.get(0)[0];

        List<Object[]> porHora = validacaoRepository
            .countByHourForRede(desde);

        List<EstatisticasLinhaResponse.AfluenciaHoraDTO> distribuicao =
            porHora.stream()
                .map(r -> EstatisticasLinhaResponse.AfluenciaHoraDTO.builder()
                    .hora(((Number) r[0]).intValue())
                    .total(((Number) r[1]).longValue())
                    .build())
                .collect(Collectors.toList());

        return EstatisticasLinhaResponse.builder()
            .linhaDesignacao(linha.getDesignacao())
            .totalViagensUltimoMes(totalViagens)
            .paragemOrigemMaisFrequente(origemMaisFrequente)
            .paragemDestinoMaisFrequente(destinoMaisFrequente)
            .distribuicaoPorHora(distribuicao)
            .build();
    }

    public EstatisticasRedeResponse estatisticasRede() {
        LocalDateTime inicioHoje = LocalDate.now()
            .atStartOfDay();
        LocalDateTime inicioOntem = inicioHoje.minusDays(1);
        LocalDateTime desde30dias = LocalDateTime.now().minusDays(30);

        Long totalHoje = validacaoRepository
            .countByPeriod(inicioHoje, LocalDateTime.now());
        Long totalOntem = validacaoRepository
            .countByPeriod(inicioOntem, inicioHoje);

        List<Object[]> porHora = validacaoRepository
            .countByHourForRede(inicioHoje);
        Integer horaPico = porHora.isEmpty() ? null
            : ((Number) porHora.get(0)[0]).intValue();

        List<Object[]> topParagensRaw = viagemRepository
            .topParagensMaisMovimentadas(desde30dias);
        List<EstatisticasRedeResponse.ParagemAfluenciaDTO> topParagens =
            topParagensRaw.stream()
                .limit(5)
                .map(r -> EstatisticasRedeResponse.ParagemAfluenciaDTO.builder()
                    .paragemNome((String) r[0])
                    .totalValidacoes(((Number) r[1]).longValue())
                    .build())
                .collect(Collectors.toList());

        List<Object[]> topLinhasRaw = viagemRepository
            .topLinhasMaisMovimentadas(desde30dias);
        List<EstatisticasRedeResponse.LinhaAfluenciaDTO> topLinhas =
            topLinhasRaw.stream()
                .limit(5)
                .map(r -> EstatisticasRedeResponse.LinhaAfluenciaDTO.builder()
                    .linhaDesignacao((String) r[0])
                    .totalViagens(((Number) r[1]).longValue())
                    .build())
                .collect(Collectors.toList());

        return EstatisticasRedeResponse.builder()
            .topParagens(topParagens)
            .topLinhas(topLinhas)
            .totalViagensHoje(totalHoje)
            .totalViagensOntem(totalOntem)
            .horaPicoRede(horaPico)
            .build();
    }
}
