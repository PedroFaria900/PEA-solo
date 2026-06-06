package pt.uminho.mei.bilhetica.entity.leitor;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.Linha;
import pt.uminho.mei.bilhetica.enums.EstadoLeitor;
import java.util.UUID;

@Entity
@Table(name = "leitor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leitor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Linha linha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLeitor estado;
}
