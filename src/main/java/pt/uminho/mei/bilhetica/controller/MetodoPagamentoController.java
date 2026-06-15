package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.MetodoRequest;
import pt.uminho.mei.bilhetica.dto.MetodoResponse;
import pt.uminho.mei.bilhetica.service.pagamento.MetodoPagamentoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carteira/metodos")
public class MetodoPagamentoController {

    private final MetodoPagamentoService metodoService;

    public MetodoPagamentoController(MetodoPagamentoService metodoService) {
        this.metodoService = metodoService;
    }

    @GetMapping
    public ResponseEntity<List<MetodoResponse>> listar(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(metodoService.listar(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<MetodoResponse> adicionar(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody MetodoRequest request) {
        return ResponseEntity.status(201).body(metodoService.adicionar(user.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoResponse> editar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @RequestBody MetodoRequest request) {
        return ResponseEntity.ok(metodoService.editar(user.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        metodoService.remover(user.getUsername(), id);
        return ResponseEntity.ok("Método de pagamento removido");
    }

    @PutMapping("/{id}/predefinido")
    public ResponseEntity<MetodoResponse> definirPredefinido(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(metodoService.definirPredefinido(user.getUsername(), id));
    }
}
