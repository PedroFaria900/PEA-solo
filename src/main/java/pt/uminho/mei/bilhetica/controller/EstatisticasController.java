package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.service.EstatisticasService;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class EstatisticasController {

    private final EstatisticasService estatisticasService;

    public EstatisticasController(EstatisticasService estatisticasService) {
        this.estatisticasService = estatisticasService;
    }

    @GetMapping("/paragens/{id}/estatisticas")
    public ResponseEntity<EstatisticasParagemResponse> estatisticasParagem(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
            estatisticasService.estatisticasParagem(id));
    }

    @GetMapping("/linhas/{id}/estatisticas")
    public ResponseEntity<EstatisticasLinhaResponse> estatisticasLinha(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
            estatisticasService.estatisticasLinha(id));
    }

    @GetMapping("/rede/estatisticas")
    public ResponseEntity<EstatisticasRedeResponse> estatisticasRede() {
        return ResponseEntity.ok(
            estatisticasService.estatisticasRede());
    }
}