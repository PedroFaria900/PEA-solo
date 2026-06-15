package pt.uminho.mei.bilhetica.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.TransacaoResponse;
import pt.uminho.mei.bilhetica.entity.Transacao;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.enums.TipoTransacao;
import pt.uminho.mei.bilhetica.repository.TransacaoRepository;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/transacoes")
    public ResponseEntity<List<TransacaoResponse>> historico(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 20, sort = "momento",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        Utente utente = utenteRepository.findByEmail(user.getUsername())
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        List<TransacaoResponse> result = transacaoRepository
            .findByUtenteId(utente.getId(), pageable)
            .stream()
            .map(t -> TransacaoResponse.builder()
                .id(t.getId())
                .valor(t.getValor())
                .tipo(t.getTipo())
                .momento(t.getMomento())
                .descricao(t.getDescricao())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
