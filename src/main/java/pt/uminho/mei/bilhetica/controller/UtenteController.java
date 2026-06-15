package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.AlterarPasswordRequest;
import pt.uminho.mei.bilhetica.dto.EditarPerfilRequest;
import pt.uminho.mei.bilhetica.dto.PerfilResponse;
import pt.uminho.mei.bilhetica.service.UtenteService;

@RestController
@RequestMapping("/api/utentes")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> perfil(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(utenteService.perfil(user.getUsername()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<PerfilResponse> atualizarPerfil(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody EditarPerfilRequest request) {
        return ResponseEntity.ok(utenteService.atualizarPerfil(
            user.getUsername(), request.getNome(), request.getTelemovel()));
    }

    @PutMapping("/perfil/password")
    public ResponseEntity<?> alterarPassword(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AlterarPasswordRequest request) {
        utenteService.alterarPassword(
            user.getUsername(), request.getPasswordAtual(), request.getPasswordNova());
        return ResponseEntity.ok("Password alterada com sucesso");
    }
}
