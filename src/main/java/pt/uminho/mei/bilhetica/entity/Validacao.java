package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.leitor.Leitor;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validacao", indexes = {
    @Index(name = "idx_validacao_titulo_momento", columnList = "titulo_id, momento"),
    @Index(name = "idx_validacao_leitor_momento", columnList = "leitor_id, momento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Validacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_id", nullable = false)
    private TituloTransporte titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @Column(nullable = false)
    private LocalDateTime momento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoValidacao resultado;
}
