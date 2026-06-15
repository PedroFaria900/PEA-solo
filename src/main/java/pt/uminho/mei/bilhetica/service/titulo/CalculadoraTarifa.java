package pt.uminho.mei.bilhetica.service.titulo;

import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;
import pt.uminho.mei.bilhetica.enums.PeriodoPasse;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import pt.uminho.mei.bilhetica.repository.TarifarioRepository;
import pt.uminho.mei.bilhetica.repository.ZonaTarifariaRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Lógica de preços e resolução de zonas partilhada pelos três tipos de título.
 * Extraído para evitar duplicação entre PasseFactory, PackFactory e BilheteFactory
 * e para centralizar a regra: "lista vazia → tarifário global (zona IS NULL)".
 */
@Component
public class CalculadoraTarifa {

    private final TarifarioRepository tarifarioRepository;
    private final ZonaTarifariaRepository zonaTarifariaRepository;

    public CalculadoraTarifa(TarifarioRepository tarifarioRepository,
                             ZonaTarifariaRepository zonaTarifariaRepository) {
        this.tarifarioRepository = tarifarioRepository;
        this.zonaTarifariaRepository = zonaTarifariaRepository;
    }

    /**
     * Preço base (sem multiplicador de viagens) para o conjunto de zonas dado.
     * {@code zonasIds} nulo/vazio → linha global (zona IS NULL no tarifário).
     * Caso contrário → soma dos preços de cada zona.
     * {@code periodo} é obrigatório para PASSE (MENSAL/ANUAL); null para PACK e BILHETE.
     */
    public BigDecimal precoBase(TipoTitulo tipo, PerfilUtente perfil, List<UUID> zonasIds, PeriodoPasse periodo) {
        if (zonasIds == null || zonasIds.isEmpty()) {
            return tarifarioRepository
                .findByAtributos(tipo, perfil, null, periodo)
                .orElseThrow(() -> new RuntimeException(
                    "Tarifário global não configurado para " + tipo))
                .getPreco();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (UUID zId : zonasIds) {
            total = total.add(
                tarifarioRepository
                    .findByAtributos(tipo, perfil, zId, periodo)
                    .orElseThrow(() -> new RuntimeException(
                        "Tarifário não configurado para a zona " + zId))
                    .getPreco()
            );
        }
        return total;
    }

    /**
     * Variante não-lançante de {@link #precoBase}: devolve {@link Optional#empty()} se
     * qualquer linha do tarifário necessária estiver em falta, em vez de lançar excepção.
     * Usado pela recomendação de títulos para saltar tipos sem tarifário configurado.
     */
    public Optional<BigDecimal> precoBaseOpcional(TipoTitulo tipo, PerfilUtente perfil, List<UUID> zonasIds, PeriodoPasse periodo) {
        if (zonasIds == null || zonasIds.isEmpty()) {
            return tarifarioRepository
                .findByAtributos(tipo, perfil, null, periodo)
                .map(t -> t.getPreco());
        }
        BigDecimal total = BigDecimal.ZERO;
        for (UUID zId : zonasIds) {
            Optional<BigDecimal> preco = tarifarioRepository
                .findByAtributos(tipo, perfil, zId, periodo)
                .map(t -> t.getPreco());
            if (preco.isEmpty()) return Optional.empty();
            total = total.add(preco.get());
        }
        return Optional.of(total);
    }

    /**
     * Resolve uma lista de IDs em entidades ZonaTarifaria.
     * Lista nula/vazia → conjunto vazio (passe-tudo / sem restrição de zona).
     */
    public Set<ZonaTarifaria> resolver(List<UUID> zonasIds) {
        if (zonasIds == null || zonasIds.isEmpty()) {
            return Set.of();
        }
        List<ZonaTarifaria> found = zonaTarifariaRepository.findAllById(zonasIds);
        if (found.size() != zonasIds.size()) {
            throw new RuntimeException("Uma ou mais zonas não encontradas");
        }
        return new HashSet<>(found);
    }
}
