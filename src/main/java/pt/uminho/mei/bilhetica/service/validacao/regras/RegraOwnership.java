package pt.uminho.mei.bilhetica.service.validacao.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ContextoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.util.Optional;

/** O título tem de pertencer ao utente autenticado. */
@Component
@Order(1)
public class RegraOwnership implements RegraValidacao {

    @Override
    public Optional<ResultadoRegra> verificar(ContextoValidacao ctx) {
        if (!ctx.titulo().getUtente().getEmail().equals(ctx.email())) {
            return Optional.of(new ResultadoRegra(
                ResultadoValidacao.INVALIDO, "Não autorizado a usar este título"));
        }
        return Optional.empty();
    }
}
