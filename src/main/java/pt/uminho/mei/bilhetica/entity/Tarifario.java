package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tarifario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tipo_titulo", nullable = false)
    private String tipoTitulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_utente", nullable = false)
    private PerfilUtente perfilUtente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaTarifaria zona;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
}
