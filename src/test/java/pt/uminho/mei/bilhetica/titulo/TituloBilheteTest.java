package pt.uminho.mei.bilhetica.titulo;

import org.junit.jupiter.api.Test;
import pt.uminho.mei.bilhetica.entity.titulo.TituloBilhete;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Janela de validade de 1h do bilhete, activada na primeira validação. */
class TituloBilheteTest {

    private final LocalDateTime t0 = LocalDateTime.of(2026, 6, 13, 10, 0, 0);

    @Test
    void naoActivado_naoExpira() {
        TituloBilhete b = new TituloBilhete();
        assertThat(b.getAtivadoEm()).isNull();
        assertThat(b.estaExpirado(t0)).isFalse();
        assertThat(b.expiraEm()).isNull();
    }

    @Test
    void primeiraValidacao_activaEAbreJanelaDeUmaHora() {
        TituloBilhete b = new TituloBilhete();

        b.registarConsumo(t0);

        assertThat(b.getAtivadoEm()).isEqualTo(t0);
        assertThat(b.expiraEm()).isEqualTo(t0.plusHours(1));
    }

    @Test
    void validacaoSeguinte_naoReiniciaAJanela() {
        TituloBilhete b = new TituloBilhete();
        b.registarConsumo(t0);

        b.registarConsumo(t0.plusMinutes(30)); // 2ª leitura dentro da hora

        assertThat(b.getAtivadoEm()).isEqualTo(t0); // janela mantém-se
    }

    @Test
    void dentroDaHora_continuaValido() {
        TituloBilhete b = new TituloBilhete();
        b.registarConsumo(t0);

        assertThat(b.estaExpirado(t0.plusMinutes(59))).isFalse();
        assertThat(b.estaExpirado(t0.plusHours(1))).isFalse(); // limite inclusive
    }

    @Test
    void aposUmaHora_expira() {
        TituloBilhete b = new TituloBilhete();
        b.registarConsumo(t0);

        assertThat(b.estaExpirado(t0.plusHours(1).plusSeconds(1))).isTrue();
    }
}
