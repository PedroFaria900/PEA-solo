package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstatisticasRedeResponse {
    private List<ParagemAfluenciaDTO> topParagens;
    private List<LinhaAfluenciaDTO> topLinhas;
    private Long totalViagensHoje;
    private Long totalViagensOntem;
    private Integer horaPicoRede;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParagemAfluenciaDTO {
        private String paragemNome;
        private Long totalValidacoes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LinhaAfluenciaDTO {
        private String linhaDesignacao;
        private Long totalViagens;
    }
}
