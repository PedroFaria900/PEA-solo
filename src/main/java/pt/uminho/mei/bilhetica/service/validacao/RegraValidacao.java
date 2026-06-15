package pt.uminho.mei.bilhetica.service.validacao;

import java.util.Optional;

/**
 * Uma regra de validação (Chain of Responsibility). As regras são beans Spring
 * ordenados por {@code @Order} e avaliadas em sequência por {@code ValidacaoService};
 * a primeira que devolva um {@link ResultadoRegra} interrompe a cadeia.
 *
 * <p>Adicionar uma nova regra resume-se a criar um novo bean que implemente esta
 * interface — sem editar o serviço nem as regras existentes.</p>
 */
public interface RegraValidacao {

    /** {@code Optional.empty()} = aprovado; presente = reprovado com o motivo dado. */
    Optional<ResultadoRegra> verificar(ContextoValidacao ctx);
}
