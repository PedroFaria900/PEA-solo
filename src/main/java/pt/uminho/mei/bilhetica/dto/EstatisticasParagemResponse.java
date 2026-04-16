package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticasParagemResponse {
    private String paragemNome;
    private Long totalValidacoes24h;
    private Integer horaPico;
    private Double mediaValidacoesPorHora;
    private List<LinhaAfluenciaDTO> topLinhas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LinhaAfluenciaDTO {
        private String linhaDesignacao;
        private Long totalViagens;
    }
}
