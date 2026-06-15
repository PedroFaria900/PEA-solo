package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "linha_paragem", indexes = {
    @Index(name = "idx_linha_paragem_seq", columnList = "linha_id, sentido, sequencia"),
    @Index(name = "idx_lp_paragem",        columnList = "paragem_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinhaParagem {
    
    @EmbeddedId
    private LinhaParagemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("linhaId")
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("paragemId")
    @JoinColumn(name = "paragem_id", nullable = false)
    private Paragem paragem;

    @Column(nullable = false)
    private Integer sequencia;

    private Integer tempoEstimadoSeg;
}
