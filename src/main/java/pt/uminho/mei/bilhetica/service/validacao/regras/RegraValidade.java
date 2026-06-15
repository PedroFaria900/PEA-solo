package pt.uminho.mei.bilhetica.service.validacao.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ContextoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.util.Optional;

/**
 * O título não pode estar expirado. Em caso de expiração marca o estado EXPIRADO
 * (o título está gerido na transacção, pelo que o dirty-checking persiste a alteração).
 */
@Component
@Order(3)
public class RegraValidade implements RegraValidacao {

    @Override
    public Optional<ResultadoRegra> verificar(ContextoValidacao ctx) {
        if (ctx.titulo().estaExpirado(ctx.agora())) {
            ctx.titulo().setEstado(EstadoTitulo.EXPIRADO);
            return Optional.of(new ResultadoRegra(
                ResultadoValidacao.INVALIDO, "Título expirado"));
        }
        return Optional.empty();
    }
}
