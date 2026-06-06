package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("PACK")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloPack extends TituloTransporte {

    @Column
    private LocalDate validade;

    @Column
    private Integer viagensRestantes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private pt.uminho.mei.bilhetica.entity.ZonaTarifaria zona;
}