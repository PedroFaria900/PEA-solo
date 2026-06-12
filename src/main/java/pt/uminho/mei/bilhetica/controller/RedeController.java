package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.enums.SentidoLinha;
import pt.uminho.mei.bilhetica.enums.TipoTransporte;
import pt.uminho.mei.bilhetica.service.RedeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RedeController {

    private final RedeService redeService;

    public RedeController(RedeService redeService) {
        this.redeService = redeService;
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

    @GetMapping("/paragens/{id}/horarios")
    public ResponseEntity<String> horarios(@PathVariable UUID id) {
        return ResponseEntity.status(501).body("Under Construction: Timetables will be implemented here.");
    }

    @GetMapping("/rotas/pesquisar")
    public ResponseEntity<String> pesquisarRotas(
            @RequestParam("origem") UUID origem,
            @RequestParam("destino") UUID destino) {
        return ResponseEntity.status(501).body("Under Construction: Trip Planner will be implemented here.");
    }

    @GetMapping("/leitores")
    public ResponseEntity<?> listarLeitores() {
        return ResponseEntity.ok(redeService.listarLeitores());
    }

    @GetMapping("/paragens")
    public ResponseEntity<?> listarParagens() {
        return ResponseEntity.ok(redeService.listarParagens());
    }
}
