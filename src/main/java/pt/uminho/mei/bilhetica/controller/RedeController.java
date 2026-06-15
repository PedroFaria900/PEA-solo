package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import pt.uminho.mei.bilhetica.enums.TipoTransporte;
import pt.uminho.mei.bilhetica.service.RedeService;
import pt.uminho.mei.bilhetica.service.RotaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RedeController {

    private final RedeService redeService;
    private final RotaService rotaService;

    public RedeController(RedeService redeService, RotaService rotaService) {
        this.redeService = redeService;
        this.rotaService = rotaService;
    }

    @GetMapping("/linhas")
    public ResponseEntity<List<LinhaResponse>> listarLinhas(
            @RequestParam(required = false) TipoTransporte tipo) {
        return ResponseEntity.ok(redeService.listarLinhas(tipo));
    }

    @GetMapping("/linhas/{id}/paragens")
    public ResponseEntity<List<LinhaParagemResponse>> paragensLinha(
            @PathVariable UUID id,
            @RequestParam(required = false) SentidoLinha sentido) {
        return ResponseEntity.ok(redeService.paragensLinha(id, sentido));
    }

    @GetMapping("/paragens")
    public ResponseEntity<List<ParagemResponse>> listarParagens() {
        return ResponseEntity.ok(redeService.listarParagens());
    }

    @GetMapping("/paragens/{id}")
    public ResponseEntity<ParagemResponse> detalheParagem(
            @PathVariable UUID id) {
        return ResponseEntity.ok(redeService.detalheParagem(id));
    }

    @GetMapping("/rotas")
    public ResponseEntity<List<RotaResponse>> sugerirRota(
            @RequestParam UUID origemId,
            @RequestParam UUID destinoId) {
        return ResponseEntity.ok(redeService.sugerirRota(origemId, destinoId));
    }

    /** Pesquisa multi-transbordo com recomendação de títulos. Requer autenticação. */
    @GetMapping("/rotas/pesquisar")
    public ResponseEntity<List<RotaPesquisaResponse>> pesquisarRota(
            @RequestParam UUID origemId,
            @RequestParam UUID destinoId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(rotaService.pesquisar(origemId, destinoId, user.getUsername()));
    }

}
