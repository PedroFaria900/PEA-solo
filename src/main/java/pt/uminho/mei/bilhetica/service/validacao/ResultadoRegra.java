package pt.uminho.mei.bilhetica.service.validacao;

import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;

/**
 * Resultado de falha de uma regra: o motivo ({@link ResultadoValidacao}) e a mensagem
 * a devolver ao cliente. Uma regra que devolve {@code Optional.empty()} considera-se aprovada.
 */
public record ResultadoRegra(ResultadoValidacao resultado, String mensagem) {}
