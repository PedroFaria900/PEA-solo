package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "zona_tarifaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZonaTarifaria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "zona_tarifaria_paragem",
        joinColumns = @JoinColumn(name = "zona_id"),
        inverseJoinColumns = @JoinColumn(name = "paragem_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Paragem> paragens;
}
