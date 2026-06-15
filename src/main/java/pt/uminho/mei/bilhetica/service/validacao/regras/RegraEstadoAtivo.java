package pt.uminho.mei.bilhetica.service.validacao.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ContextoValidacao;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.util.Optional;

/** O título tem de estar no estado ATIVO. */
@Component
@Order(2)
public class RegraEstadoAtivo implements RegraValidacao {

    @Override
    public Optional<ResultadoRegra> verificar(ContextoValidacao ctx) {
        if (ctx.titulo().getEstado() != EstadoTitulo.ATIVO) {
            return Optional.of(new ResultadoRegra(
                ResultadoValidacao.INVALIDO, "Título não está activo"));
        }
        return Optional.empty();
    }
}
