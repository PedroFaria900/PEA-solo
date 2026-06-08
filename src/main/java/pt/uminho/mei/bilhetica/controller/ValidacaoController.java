package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.service.ValidacaoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/validacoes")
public class ValidacaoController {

    private final ValidacaoService validacaoService;

    public ValidacaoController(ValidacaoService validacaoService) {
        this.validacaoService = validacaoService;
    }

    @PostMapping
    public ResponseEntity<ValidacaoResponse> validar(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user,
            @RequestBody ValidacaoRequest request) {
        return ResponseEntity.ok(
            validacaoService.processar(request, user.getUsername()));
    }
}
