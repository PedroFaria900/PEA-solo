package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.entity.*;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;
import pt.uminho.mei.bilhetica.enums.PeriodoPasse;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.service.titulo.CalculadoraTarifa;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pesquisa de rotas multi-transobordo e recomendação de títulos.
 *
 * Algoritmo: BFS sobre o grafo linha-paragem, com conjunto de paragens
 * visitadas por caminho (previne ciclos). Simplificação documentada: um
 * caminho que chega primeiro a uma paragem "reserva" esse ramo — percursos
 * alternativos de igual transferência que passem pela mesma paragem podem ser
 * ignorados. Aceitável para o projecto; apontado no relatório.
 */
@Service
public class RotaService {

    /** Máximo de pernas (legs) por rota: 3 pernas = 2 transbordos. */
    private static final int MAX_LEGS = 3;
    /** Máximo de rotas devolvidas. */
    private static final int MAX_RESULTS = 5;

    private final LinhaParagemRepository linhaParagemRepository;
    private final ZonaTarifariaRepository zonaTarifariaRepository;
    private final UtenteRepository utenteRepository;
    private final CalculadoraTarifa calculadoraTarifa;
    private final PackTierService packTierService;

    public RotaService(LinhaParagemRepository linhaParagemRepository,
                       ZonaTarifariaRepository zonaTarifariaRepository,
                       UtenteRepository utenteRepository,
                       CalculadoraTarifa calculadoraTarifa,
                       PackTierService packTierService) {
        this.linhaParagemRepository = linhaParagemRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
        this.utenteRepository = utenteRepository;
        this.calculadoraTarifa = calculadoraTarifa;
        this.packTierService = packTierService;
    }

    // -------------------------------------------------------------------------
    // Internal leg record (used only during BFS; never exposed directly)
    // -------------------------------------------------------------------------
    private static class Leg {
        final UUID linhaId;
        final String linhaDesignacao;
        final SentidoLinha sentido;
        final Paragem embarque;
        final Paragem desembarque;
        final int tempoSeg;
        final int numParagens;
        final List<UUID> stopIds; // embarque inclusive, desembarque inclusive

        Leg(UUID linhaId, String linhaDesignacao, SentidoLinha sentido,
            Paragem embarque, Paragem desembarque,
            int tempoSeg, int numParagens, List<UUID> stopIds) {
            this.linhaId = linhaId;
            this.linhaDesignacao = linhaDesignacao;
            this.sentido = sentido;
            this.embarque = embarque;
            this.desembarque = desembarque;
            this.tempoSeg = tempoSeg;
            this.numParagens = numParagens;
            this.stopIds = stopIds;
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public List<RotaPesquisaResponse> pesquisar(UUID origemId, UUID destinoId, String email) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
        PerfilUtente perfil = utente.getPerfil();

        // Caches para evitar queries repetidas na mesma pesquisa
        Map<String, List<LinhaParagem>> seqCache = new HashMap<>();     // "linhaId:sentido" -> sequência
        Map<UUID, List<LinhaParagem>> paragemCache = new HashMap<>();   // paragemId -> linhas que passam

        List<List<Leg>> rotasEncontradas = new ArrayList<>();

        // Estado BFS: (paragemActual, pernas acumuladas, paragens visitadas, última linha+sentido)
        record Estado(UUID stop, List<Leg> legs, Set<UUID> visited, String lastLinhaDir) {}
        Deque<Estado> queue = new ArrayDeque<>();
        queue.add(new Estado(origemId, new ArrayList<>(), new HashSet<>(Set.of(origemId)), null));

        while (!queue.isEmpty() && rotasEncontradas.size() < MAX_RESULTS) {
            Estado estado = queue.poll();
            if (estado.legs().size() >= MAX_LEGS) continue;

            List<LinhaParagem> linhasNaParagem = paragemCache.computeIfAbsent(
                estado.stop(), linhaParagemRepository::findByParagemId);

            for (LinhaParagem lpOrigem : linhasNaParagem) {
                String linhaDir = lpOrigem.getLinha().getId() + ":" + lpOrigem.getId().getSentido();
                // Não reembarcar na mesma linha+sentido imediatamente após sair dela
                if (linhaDir.equals(estado.lastLinhaDir())) continue;

                List<LinhaParagem> seq = seqCache.computeIfAbsent(linhaDir, k ->
                    linhaParagemRepository.findByLinhaIdAndSentidoOrdenado(
                        lpOrigem.getLinha().getId(), lpOrigem.getId().getSentido()));

                // Localizar a paragem actual na sequência
                int idxOrigem = -1;
                for (int i = 0; i < seq.size(); i++) {
                    if (seq.get(i).getParagem().getId().equals(estado.stop())) {
                        idxOrigem = i;
                        break;
                    }
                }
                if (idxOrigem < 0) continue;

                // Explorar todas as paragens a jusante
                for (int i = idxOrigem + 1; i < seq.size(); i++) {
                    UUID destStopId = seq.get(i).getParagem().getId();

                    int tempoSeg = seq.subList(idxOrigem, i).stream()
                        .mapToInt(lp -> lp.getTempoEstimadoSeg() != null ? lp.getTempoEstimadoSeg() : 0)
                        .sum();

                    List<UUID> legStopIds = seq.subList(idxOrigem, i + 1).stream()
                        .map(lp -> lp.getParagem().getId())
                        .collect(Collectors.toList());

                    Leg leg = new Leg(
                        lpOrigem.getLinha().getId(),
                        lpOrigem.getLinha().getDesignacao(),
                        lpOrigem.getId().getSentido(),
                        seq.get(idxOrigem).getParagem(),
                        seq.get(i).getParagem(),
                        tempoSeg,
                        i - idxOrigem,
                        legStopIds
                    );

                    List<Leg> newLegs = new ArrayList<>(estado.legs());
                    newLegs.add(leg);

                    if (destStopId.equals(destinoId)) {
                        rotasEncontradas.add(newLegs);
                        if (rotasEncontradas.size() >= MAX_RESULTS) break;
                    } else if (!estado.visited().contains(destStopId) && newLegs.size() < MAX_LEGS) {
                        Set<UUID> newVisited = new HashSet<>(estado.visited());
                        newVisited.add(destStopId);
                        queue.add(new Estado(destStopId, newLegs, newVisited, linhaDir));
                    }
                }
                if (rotasEncontradas.size() >= MAX_RESULTS) break;
            }
        }

        // Menos transbordos primeiro; a tempo igual, viagem mais rápida
        rotasEncontradas.sort(Comparator
            .comparingInt((List<Leg> r) -> r.size())
            .thenComparingInt(r -> r.stream().mapToInt(l -> l.tempoSeg).sum()));

        return rotasEncontradas.stream()
            .map(legs -> toResponse(legs, perfil))
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Response building
    // -------------------------------------------------------------------------

    private RotaPesquisaResponse toResponse(List<Leg> legs, PerfilUtente perfil) {
        int totalTempo = legs.stream().mapToInt(l -> l.tempoSeg).sum();

        List<LegRotaResponse> legResponses = legs.stream()
            .map(leg -> LegRotaResponse.builder()
                .linhaId(leg.linhaId)
                .linhaDesignacao(leg.linhaDesignacao)
                .sentido(leg.sentido)
                .paragemEmbarque(toParagemResponse(leg.embarque))
                .paragemSaida(toParagemResponse(leg.desembarque))
                .tempoEstimadoSeg(leg.tempoSeg)
                .numParagens(leg.numParagens)
                .build())
            .collect(Collectors.toList());

        // Todas as paragens percorridas em todas as pernas
        Set<UUID> allStopIds = legs.stream()
            .flatMap(l -> l.stopIds.stream())
            .collect(Collectors.toSet());

        // Zonas atravessadas (query inversa: paragens → zonas)
        List<ZonaTarifaria> zonas = allStopIds.isEmpty()
            ? Collections.emptyList()
            : zonaTarifariaRepository.findByParagemIdsIn(allStopIds);

        List<String> zonasNomes = zonas.stream()
            .map(ZonaTarifaria::getNome)
            .sorted()
            .collect(Collectors.toList());

        List<UUID> zonasIds = zonas.stream()
            .map(ZonaTarifaria::getId)
            .collect(Collectors.toList());

        List<RecomendacaoTituloResponse> recomendacoes = computeRecomendacoes(zonasIds, zonasNomes, perfil);

        return RotaPesquisaResponse.builder()
            .legs(legResponses)
            .tempoTotalSeg(totalTempo)
            .numTransbordos(legs.size() - 1)
            .zonasAtravessadas(zonasNomes)
            .recomendacoes(recomendacoes)
            .build();
    }

    private List<RecomendacaoTituloResponse> computeRecomendacoes(
            List<UUID> zonasIds, List<String> zonasNomes, PerfilUtente perfil) {

        List<RecomendacaoTituloResponse> recomendacoes = new ArrayList<>();

        for (TipoTitulo tipo : TipoTitulo.values()) {
            // Para PASSE recomenda-se sempre o período MENSAL. Anual pode ser comprado directamente.
            PeriodoPasse periodo = (tipo == TipoTitulo.PASSE) ? PeriodoPasse.MENSAL : null;
            // Preço para o conjunto exacto de zonas da rota
            Optional<BigDecimal> precoZonas = calculadoraTarifa.precoBaseOpcional(tipo, perfil, zonasIds, periodo);
            // Preço passe-tudo (linha global do tarifário, zona IS NULL)
            Optional<BigDecimal> precoGlobal = calculadoraTarifa.precoBaseOpcional(tipo, perfil, List.of(), periodo);

            if (precoZonas.isEmpty() && precoGlobal.isEmpty()) continue; // tipo sem tarifário

            // Escolher a opção mais barata disponível; global ganha em empate (cobre mais)
            boolean passeTudo;
            BigDecimal precoEscolhido;
            List<UUID> zonasEscolhidas;
            List<String> nomesEscolhidos;

            if (zonasIds.isEmpty()) {
                // Rota sem zonas mapeadas → recomenda sempre passe-tudo
                passeTudo = true;
                precoEscolhido = precoGlobal.orElseGet(precoZonas::get);
                zonasEscolhidas = List.of();
                nomesEscolhidos = List.of("Rede completa");
            } else if (precoZonas.isPresent() && precoGlobal.isPresent()) {
                passeTudo = precoGlobal.get().compareTo(precoZonas.get()) <= 0;
                if (passeTudo) {
                    precoEscolhido = precoGlobal.get();
                    zonasEscolhidas = List.of();
                    nomesEscolhidos = List.of("Rede completa");
                } else {
                    precoEscolhido = precoZonas.get();
                    zonasEscolhidas = zonasIds;
                    nomesEscolhidos = zonasNomes;
                }
            } else if (precoZonas.isPresent()) {
                passeTudo = false;
                precoEscolhido = precoZonas.get();
                zonasEscolhidas = zonasIds;
                nomesEscolhidos = zonasNomes;
            } else {
                passeTudo = true;
                precoEscolhido = precoGlobal.get();
                zonasEscolhidas = List.of();
                nomesEscolhidos = List.of("Rede completa");
            }

            if (tipo == TipoTitulo.PACK) {
                // Pack: mostrar preço por viagem + tiers da base de dados
                List<PackTierResponse> tiers = packTierService.listar().stream()
                    .map(t -> PackTierResponse.builder()
                        .viagens(t)
                        .precoTotal(precoEscolhido.multiply(BigDecimal.valueOf(t)))
                        .build())
                    .collect(Collectors.toList());

                recomendacoes.add(RecomendacaoTituloResponse.builder()
                    .tipo(tipo)
                    .passeTudo(passeTudo)
                    .zonasIds(zonasEscolhidas)
                    .zonasNomes(nomesEscolhidos)
                    .preco(null)
                    .precoPorViagem(precoEscolhido)
                    .tiers(tiers)
                    .build());
            } else {
                recomendacoes.add(RecomendacaoTituloResponse.builder()
                    .tipo(tipo)
                    .passeTudo(passeTudo)
                    .zonasIds(zonasEscolhidas)
                    .zonasNomes(nomesEscolhidos)
                    .preco(precoEscolhido)
                    .precoPorViagem(null)
                    .tiers(null)
                    .build());
            }
        }

        return recomendacoes;
    }

    private ParagemResponse toParagemResponse(Paragem p) {
        return ParagemResponse.builder()
            .id(p.getId())
            .nome(p.getNome())
            .codigo(p.getCodigo())
            .municipio(p.getMunicipio())
            .build();
    }
}
