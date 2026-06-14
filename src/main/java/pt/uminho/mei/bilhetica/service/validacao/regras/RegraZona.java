package pt.uminho.mei.bilhetica.service.validacao.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.repository.ZonaTarifariaRepository;
import pt.uminho.mei.bilhetica.service.validacao.ContextoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A linha do leitor tem de pertencer a (pelo menos) uma das zonas do título.
 * Título sem zonas = passe-tudo (sem restrição geográfica).
 */
@Component
@Order(5)
public class RegraZona implements RegraValidacao {

    private final ZonaTarifariaRepository zonaTarifariaRepository;

    public RegraZona(ZonaTarifariaRepository zonaTarifariaRepository) {
        this.zonaTarifariaRepository = zonaTarifariaRepository;
    }

    @Override
    public Optional<ResultadoRegra> verificar(ContextoValidacao ctx) {
        Set<ZonaTarifaria> zonas = ctx.titulo().zonasAbrangidas();
        if (zonas.isEmpty()) {
            return Optional.empty(); // passe-tudo
        }
        UUID linhaId = ctx.leitor().getLinha().getId();
        for (ZonaTarifaria zona : zonas) {
            if (zonaTarifariaRepository.isLinhaInZona(zona.getId(), linhaId)) {
                return Optional.empty();
            }
        }
        return Optional.of(new ResultadoRegra(
            ResultadoValidacao.FORA_ZONA, "Linha fora da área do título"));
    }
}
