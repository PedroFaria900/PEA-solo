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
@RequestMapping("/api/carteira")
public class CarteiraController {

    private final UtenteRepository utenteRepository;
    private final TransacaoRepository transacaoRepository;

    public CarteiraController(UtenteRepository utenteRepository,
                              TransacaoRepository transacaoRepository) {
        this.utenteRepository = utenteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @GetMapping
    public ResponseEntity<?> obterSaldo(@AuthenticationPrincipal UserDetails user) {
        Utente utente = utenteRepository.findByEmail(user.getUsername())
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        return ResponseEntity.ok(Map.of("saldo", utente.getSaldo()));
    }

    @PostMapping("/carregamentos")
    public ResponseEntity<?> carregarSaldo(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Map<String, BigDecimal> body) {

        BigDecimal valor = body.get("valor");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor inválido");
        }

        Utente utente = utenteRepository.findByEmail(user.getUsername())
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        utente.setSaldo(utente.getSaldo().add(valor));
        utenteRepository.save(utente);

        transacaoRepository.save(Transacao.builder()
            .utente(utente)
            .valor(valor)
            .tipo(TipoTransacao.CARREGAMENTO)
            .momento(LocalDateTime.now())
            .descricao("Carregamento de saldo")
            .build());

        return ResponseEntity.ok(Map.of("saldo", utente.getSaldo()));
    }
}
