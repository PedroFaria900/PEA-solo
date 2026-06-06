package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;

@Entity
@DiscriminatorValue("BILHETE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloBilhete extends TituloTransporte {

    @Column
    private LocalDate validade;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "titulo_bilhete_zona",
        joinColumns = @JoinColumn(name = "titulo_bilhete_id"),
        inverseJoinColumns = @JoinColumn(name = "zona_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ZonaTarifaria> zonas;
}
