package pt.uminho.mei.bilhetica.enums;

/**
 * Discriminador de negócio para os métodos de pagamento.
 * Coincide exactamente com os @DiscriminatorValue das subclasses.
 * Adicionar um novo método = nova subclasse + novo valor aqui + nova factory.
 */
public enum TipoMetodoPagamento {
    CARTAO, MBWAY
}
