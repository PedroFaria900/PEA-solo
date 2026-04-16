package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.service.TituloService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/titulos")
public class TituloController {

    private final TituloService tituloService;

    public TituloController(TituloService tituloService) {
        this.tituloService = tituloService;
    }

    @GetMapping
    public ResponseEntity<List<TituloResponse>> listar(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
            tituloService.listarTitulos(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TituloResponse> detalhe(
            @PathVariable UUID id) {
        return ResponseEntity.ok(tituloService.detalhe(id));
    }

    @PostMapping("/comprar")
    public ResponseEntity<TituloResponse> comprar(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ComprarTituloRequest request) {
        return ResponseEntity.status(201).body(
            tituloService.comprar(user.getUsername(), request));
    }

    @GetMapping("/{id}/token")
    public ResponseEntity<TokenResponse> gerarToken(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(
            tituloService.gerarToken(user.getUsername(), id));
    }
}
