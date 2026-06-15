package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.PeriodoPasse;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("PASSE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloPasse extends TituloTransporte {

    @Column
    private LocalDate validade;

    /** Período de validade (MENSAL/ANUAL). Persistido para apresentação e relatório. */
    @Enumerated(EnumType.STRING)
    @Column(name = "periodo")
    private PeriodoPasse periodo;

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.PASSE;
    }

    @Override
    public boolean estaExpirado(LocalDateTime agora) {
        return validade != null && validade.isBefore(agora.toLocalDate());
    }

    @Override
    public boolean temSaldoDisponivel() {
        return true;
    }

    @Override
    public void registarConsumo(LocalDateTime agora) {
        // Passe não consome saldo nem viagens.
    }

    @Override
    public Integer viagensRestantesResponse() {
        return null;
    }

    @Override
    public PeriodoPasse periodoResponse() {
        return periodo;
    }

    @Override
    public LocalDateTime expiraEm() {
        return validade != null ? validade.plusDays(1).atStartOfDay() : null;
    }
}
