package pt.uminho.mei.bilhetica.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacaoRequest {
    private UUID tituloId;
    /** Código do leitor lido a partir do QR afixado no veículo (ex.: SER_52f1-BUS01). */
    private String leitorCodigo;
}
