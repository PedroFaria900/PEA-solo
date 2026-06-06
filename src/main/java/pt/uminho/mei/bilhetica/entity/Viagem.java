package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "viagem", indexes = {
    @Index(name = "idx_viagem_validacao", columnList = "validacao_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validacao_id", nullable = false)
    private Validacao validacao;

    @Column(nullable = false)
    private LocalDateTime momento;
}
