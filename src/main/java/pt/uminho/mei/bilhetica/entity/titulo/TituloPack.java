package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.entity.ZonaTarifaria;
import pt.uminho.mei.bilhetica.enums.EstadoTitulo;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
    private ZonaTarifaria zona;

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.PACK;
    }

    @Override
    public Set<ZonaTarifaria> zonasAbrangidas() {
        return zona != null ? Set.of(zona) : Set.of();
    }

    @Override
    public boolean estaExpirado(LocalDateTime agora) {
        return validade != null && validade.isBefore(agora.toLocalDate());
    }

    @Override
    public boolean temSaldoDisponivel() {
        return viagensRestantes != null && viagensRestantes > 0;
    }

    @Override
    public void registarConsumo(LocalDateTime agora) {
        if (viagensRestantes != null) {
            viagensRestantes--;
            if (viagensRestantes <= 0) {
                setEstado(EstadoTitulo.ESGOTADO);
            }
        }
    }

    @Override
    public Integer viagensRestantesResponse() {
        return viagensRestantes;
    }

    @Override
    public String areaGeografica() {
        return zona != null ? zona.getNome() : null;
    }

    @Override
    public LocalDateTime expiraEm() {
        return validade != null ? validade.plusDays(1).atStartOfDay() : null;
    }
}
