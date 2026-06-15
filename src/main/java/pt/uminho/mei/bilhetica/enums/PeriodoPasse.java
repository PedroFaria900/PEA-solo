package pt.uminho.mei.bilhetica.enums;

import java.time.LocalDate;

/**
 * Período de validade de um passe.
 * O servidor calcula a data de validade a partir do período — o cliente não envia {@code validade}.
 */
public enum PeriodoPasse {

    MENSAL(1),
    ANUAL(12);

    /** Número de meses de validade a contar da data de compra. */
    public final int meses;

    PeriodoPasse(int meses) {
        this.meses = meses;
    }

    /**
     * Calcula a data de fim de validade a partir de uma data de início.
     * Exemplo: MENSAL.validadeDesde(2026-06-15) → 2026-07-15
     */
    public LocalDate validadeDesde(LocalDate inicio) {
        return inicio.plusMonths(meses);
    }
}
