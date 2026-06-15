package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.PeriodoPasse;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "titulo_transporte")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_titulo", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TituloTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column
    private EstadoTitulo estado;

    @Version
    private Long version;

    /** Conjunto de zonas tarifárias cobertas. Vazio = sem restrição (passe-tudo). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "titulo_zona",
        joinColumns = @JoinColumn(name = "titulo_id"),
        inverseJoinColumns = @JoinColumn(name = "zona_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ZonaTarifaria> zonas;

    // ─────────────────────────────────────────────────────────────
    // Comportamento polimórfico — cada subtipo decide as suas regras.
    // Adicionar um novo tipo de título = nova subclasse que implementa
    // estes métodos, sem editar serviços ou regras de validação.
    // ─────────────────────────────────────────────────────────────

    /** Discriminador de negócio do título (coincide com o @DiscriminatorValue). */
    public abstract TipoTitulo tipo();

    /** Zonas que o título cobre. Conjunto vazio = sem restrição de zona (passe-tudo). */
    public Set<ZonaTarifaria> zonasAbrangidas() {
        return zonas != null ? zonas : Set.of();
    }

    /** Indica se o título já não é válido no instante dado. */
    public abstract boolean estaExpirado(LocalDateTime agora);

    /** Indica se ainda há saldo/viagens para consumir (relevante para packs). */
    public abstract boolean temSaldoDisponivel();

    /**
     * Aplica o efeito de uma validação bem-sucedida no próprio título
     * (ex.: pack decrementa viagens; bilhete activa-se na 1ª utilização).
     */
    public abstract void registarConsumo(LocalDateTime agora);

    // ── Auxiliares para mapeamento de resposta (evitam instanceof no serviço) ──

    /** Viagens restantes para apresentação (null quando não aplicável). */
    public abstract Integer viagensRestantesResponse();

    /** Período do passe para apresentação (null quando não aplicável). Sobrepostos por TituloPasse. */
    public PeriodoPasse periodoResponse() { return null; }

    /** Descrição textual da área geográfica coberta. Vazio/null → "Rede completa" (passe-tudo). */
    public String areaGeografica() {
        if (zonas == null || zonas.isEmpty()) {
            return "Rede completa";
        }
        return zonas.stream().map(ZonaTarifaria::getNome).collect(Collectors.joining(", "));
    }

    /** Instante de expiração para apresentação (null se ainda não determinado). */
    public abstract LocalDateTime expiraEm();
}
