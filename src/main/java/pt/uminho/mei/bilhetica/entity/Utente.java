package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "utente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    private String telemovel; 

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PerfilUtente perfil = PerfilUtente.NORMAL;

    /** Se true, este utente tem permissões de administrador (ROLE_ADMIN). */
    @Column(nullable = false)
    @Builder.Default
    private boolean admin = false;

    @Version
    private Long version;
}
