package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import pt.uminho.mei.bilhetica.enums.TipoTransporte;
import pt.uminho.mei.bilhetica.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedeService {

    private final LinhaRepository linhaRepository;
    private final ParagemRepository paragemRepository;
    private final LinhaParagemRepository linhaParagemRepository;

    public RedeService(LinhaRepository linhaRepository,
                       ParagemRepository paragemRepository,
                       LinhaParagemRepository linhaParagemRepository) {
        this.linhaRepository = linhaRepository;
        this.paragemRepository = paragemRepository;
        this.linhaParagemRepository = linhaParagemRepository;
    }

    public List<LinhaResponse> listarLinhas(TipoTransporte tipo) {
        List<Linha> linhas = tipo != null
            ? linhaRepository.findByTipoTransporte(tipo)
            : linhaRepository.findAll();

        return linhas.stream()
            .map(l -> LinhaResponse.builder()
                .id(l.getId())
                .designacao(l.getDesignacao())
                .tipoTransporte(l.getTipoTransporte())
                .build())
            .collect(Collectors.toList());
    }

    public ParagemResponse detalheParagem(UUID id) {
        Paragem p = paragemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paragem não encontrada"));

        return ParagemResponse.builder()
            .id(p.getId())
            .nome(p.getNome())
            .codigo(p.getCodigo())
            .municipio(p.getMunicipio())
            .latitude(p.getLatitude())
            .longitude(p.getLongitude())
            .build();
    }

    public List<LinhaParagemResponse> paragensLinha(UUID linhaId,
                                                     SentidoLinha sentido) {
        SentidoLinha s = sentido != null ? sentido : SentidoLinha.IDA;

        return linhaParagemRepository
            .findByLinhaIdAndSentidoOrdenado(linhaId, s)
            .stream()
            .map(lp -> LinhaParagemResponse.builder()
                .paragemId(lp.getParagem().getId())
                .paragemNome(lp.getParagem().getNome())
                .paragemCodigo(lp.getParagem().getCodigo())
                .latitude(lp.getParagem().getLatitude())
                .longitude(lp.getParagem().getLongitude())
                .sentido(lp.getId().getSentido())
                .sequencia(lp.getSequencia())
                .distanciaMetros(lp.getDistanciaMetros())
                .tempoEstimadoSeg(lp.getTempoEstimadoSeg())
                .build())
            .collect(Collectors.toList());
    }

    public List<RotaResponse> sugerirRota(UUID origemId, UUID destinoId) {
        List<RotaResponse> rotas = new ArrayList<>();

        List<LinhaParagem> linhasOrigem =
            linhaParagemRepository.findByParagemId(origemId);

        for (LinhaParagem lpOrigem : linhasOrigem) {
            UUID linhaId = lpOrigem.getLinha().getId();
            SentidoLinha sentido = lpOrigem.getId().getSentido();

            List<LinhaParagem> sequencia =
                linhaParagemRepository
                    .findByLinhaIdAndSentidoOrdenado(linhaId, sentido);

            int idxOrigem = -1, idxDestino = -1;
            for (int i = 0; i < sequencia.size(); i++) {
                UUID pId = sequencia.get(i).getParagem().getId();
                if (pId.equals(origemId)) idxOrigem = i;
                if (pId.equals(destinoId)) idxDestino = i;
            }

            if (idxOrigem >= 0 && idxDestino > idxOrigem) {
                int tempo = sequencia.subList(idxOrigem, idxDestino)
                    .stream()
                    .mapToInt(lp -> lp.getTempoEstimadoSeg() != null
                        ? lp.getTempoEstimadoSeg() : 0)
                    .sum();

                Paragem origem = sequencia.get(idxOrigem).getParagem();
                Paragem destino = sequencia.get(idxDestino).getParagem();

                rotas.add(RotaResponse.builder()
                    .linhaId(linhaId)
                    .linhaDesignacao(lpOrigem.getLinha().getDesignacao())
                    .paragemEmbarque(ParagemResponse.builder()
                        .id(origem.getId())
                        .nome(origem.getNome())
                        .codigo(origem.getCodigo())
                        .latitude(origem.getLatitude())
                        .longitude(origem.getLongitude())
                        .build())
                    .paragemSaida(ParagemResponse.builder()
                        .id(destino.getId())
                        .nome(destino.getNome())
                        .codigo(destino.getCodigo())
                        .latitude(destino.getLatitude())
                        .longitude(destino.getLongitude())
                        .build())
                    .tempoEstimadoSeg(tempo)
                    .numerParagens(idxDestino - idxOrigem)
                    .build());
            }
        }

        return rotas;
    }
}
