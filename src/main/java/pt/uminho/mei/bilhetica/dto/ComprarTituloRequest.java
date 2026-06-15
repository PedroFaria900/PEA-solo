package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprarTituloRequest {
    private TipoTitulo tipo;
    private LocalDate validade; // Passe e Pack (Bilhete activa-se na 1ª validação)
    private List<UUID> zonasIds; // Zonas cobertas; vazio/null = passe-tudo (todos os tipos)
    private Integer viagens;    // Obrigatório para Pack
}
