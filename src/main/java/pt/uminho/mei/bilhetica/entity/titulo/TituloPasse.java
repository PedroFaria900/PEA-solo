package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("PASSE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloPasse extends TituloTransporte {

    @Column
    private LocalDate validade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private pt.uminho.mei.bilhetica.entity.ZonaTarifaria zona;
}