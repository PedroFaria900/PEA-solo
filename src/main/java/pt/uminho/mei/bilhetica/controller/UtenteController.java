package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.entity.Transacao;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.enums.TipoTransacao;
import pt.uminho.mei.bilhetica.repository.TransacaoRepository;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/utentes")
public class UtenteController {

    private final UtenteRepository utenteRepository;
    private final TransacaoRepository transacaoRepository;

    public UtenteController(UtenteRepository utenteRepository,
                            TransacaoRepository transacaoRepository) {
        this.utenteRepository = utenteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(@AuthenticationPrincipal UserDetails user) {
        Utente utente = utenteRepository.findByEmail(user.getUsername())
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        return ResponseEntity.ok(Map.of(
            "id", utente.getId(),
            "nome", utente.getNome(),
            "email", utente.getEmail(),
            "saldo", utente.getSaldo()
        ));
    }


}
