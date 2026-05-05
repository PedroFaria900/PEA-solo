package pt.uminho.mei.bilhetica.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinhaParagemId implements Serializable {
    private UUID linhaId;
    private UUID paragemId;

    @Enumerated(EnumType.STRING)
    private SentidoLinha sentido;
}
