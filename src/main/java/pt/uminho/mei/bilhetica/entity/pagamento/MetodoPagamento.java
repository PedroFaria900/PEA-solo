package pt.uminho.mei.bilhetica.entity.pagamento;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;

import java.util.UUID;

/**
 * Base SINGLE_TABLE para métodos de pagamento.
 * Espelha o padrão de TituloTransporte: sem instanceof nos serviços,
 * comportamento polimórfico via métodos abstractos.
 * Adicionar um novo tipo = nova subclasse + CartaoFactory/MbwayFactory par.
 */
@Entity
@Table(name = "metodo_pagamento")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_metodo", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    private boolean predefinido;

    @Version
    private Long version;

    /** Discriminador de negócio (coincide com @DiscriminatorValue). */
    public abstract TipoMetodoPagamento tipo();

    /**
     * Representação mascarada para apresentação ao utilizador
     * (ex.: "Visa ****1234", "MBWay 912345678").
     */
    public abstract String resumo();
}
