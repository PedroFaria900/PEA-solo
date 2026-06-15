package pt.uminho.mei.bilhetica.service.validacao.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ContextoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.util.Optional;

/** Tem de haver saldo/viagens disponíveis (relevante para packs). */
@Component
@Order(4)
public class RegraSaldo implements RegraValidacao {

    @Override
    public Optional<ResultadoRegra> verificar(ContextoValidacao ctx) {
        if (!ctx.titulo().temSaldoDisponivel()) {
            return Optional.of(new ResultadoRegra(
                ResultadoValidacao.SEM_SALDO, "Pack sem viagens disponíveis"));
        }
        return Optional.empty();
    }
}
