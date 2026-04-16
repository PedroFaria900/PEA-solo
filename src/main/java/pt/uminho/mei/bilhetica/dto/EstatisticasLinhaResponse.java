package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticasLinhaResponse {
    private String linhaDesignacao;
    private Long totalViagensUltimoMes;
    private String paragemOrigemMaisFrequente;
    private String paragemDestinoMaisFrequente;
    private List<AfluenciaHoraDTO> distribuicaoPorHora;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AfluenciaHoraDTO {
        private Integer hora;
        private Long total;
    }
}
