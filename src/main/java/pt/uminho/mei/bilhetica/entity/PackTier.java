package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Quantidade de viagens disponível para compra de um pack.
 * Gerida em base de dados para ser editável sem recompilação.
 */
@Entity
@Table(name = "pack_tier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private int viagens;
}
