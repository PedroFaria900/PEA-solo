package pt.uminho.mei.bilhetica.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.CarregamentoRequest;
import pt.uminho.mei.bilhetica.dto.SaldoResponse;
import pt.uminho.mei.bilhetica.dto.TransacaoResponse;
import pt.uminho.mei.bilhetica.service.CarteiraService;

import java.util.List;

@RestController
@RequestMapping("/api/carteira")
public class CarteiraController {

    private final CarteiraService carteiraService;

    public CarteiraController(CarteiraService carteiraService) {
        this.carteiraService = carteiraService;
    }

    @GetMapping
    public ResponseEntity<SaldoResponse> obterSaldo(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(carteiraService.obterSaldo(user.getUsername()));
    }

    @PostMapping("/carregamentos")
    public ResponseEntity<SaldoResponse> carregarSaldo(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CarregamentoRequest request) {
        return ResponseEntity.ok(carteiraService.carregar(user.getUsername(), request));
    }

    @GetMapping("/transacoes")
    public ResponseEntity<List<TransacaoResponse>> historico(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 20, sort = "momento",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(carteiraService.historico(user.getUsername(), pageable));
    }
}
