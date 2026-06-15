package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.PeriodoPasse;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprarTituloRequest {
    private TipoTitulo tipo;
    /**
     * Período do passe: MENSAL ou ANUAL. Obrigatório para PASSE; ignorado noutros tipos.
     * O servidor calcula a data de validade a partir do período — não enviar {@code validade} para PASSE.
     */
    private PeriodoPasse periodo;
    private LocalDate validade; // Pack (Bilhete activa-se na 1ª validação; Passe usa periodo)
    private List<UUID> zonasIds; // Zonas cobertas; vazio/null = passe-tudo (todos os tipos)
    private Integer viagens;    // Obrigatório para Pack
}
