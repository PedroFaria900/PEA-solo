package pt.uminho.mei.bilhetica.entity.titulo;

import jakarta.persistence.*;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("BILHETE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TituloBilhete extends TituloTransporte {

    /**
     * Instante da primeira validação. O bilhete só começa a contar a janela
     * de 1 hora quando é usado pela primeira vez; null = ainda não activado.
     */
    @Column(name = "ativado_em")
    private LocalDateTime ativadoEm;

    /** Janela de validade de um bilhete após activação. */
    private static final long DURACAO_HORAS = 1;

    @Override
    public TipoTitulo tipo() {
        return TipoTitulo.BILHETE;
    }

    @Override
    public boolean estaExpirado(LocalDateTime agora) {
        return ativadoEm != null && agora.isAfter(ativadoEm.plusHours(DURACAO_HORAS));
    }

    @Override
    public boolean temSaldoDisponivel() {
        return true;
    }

    @Override
    public void registarConsumo(LocalDateTime agora) {
        if (ativadoEm == null) {
            ativadoEm = agora; // activação na 1ª validação → janela de 1h
        }
    }

    @Override
    public Integer viagensRestantesResponse() {
        return null;
    }

    @Override
    public LocalDateTime expiraEm() {
        return ativadoEm != null ? ativadoEm.plusHours(DURACAO_HORAS) : null;
    }
}
