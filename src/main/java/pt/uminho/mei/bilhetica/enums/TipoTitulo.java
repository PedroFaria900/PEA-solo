package pt.uminho.mei.bilhetica.enums;

/**
 * Tipos de título de transporte suportados.
 * O nome de cada constante coincide com o {@code @DiscriminatorValue} da subclasse
 * correspondente em {@code entity.titulo} e com o valor textual da coluna
 * {@code tipo_titulo} na tabela {@code tarifario}.
 */
public enum TipoTitulo {
    PASSE, PACK, BILHETE
}
