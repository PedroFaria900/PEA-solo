package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.LeitorResponse;
import pt.uminho.mei.bilhetica.service.LeitorService;

import java.util.Base64;

/**
 * Endpoints de leitura sobre os leitores (validadores) instalados nos veículos.
 * O telemóvel resolve o código lido no QR do veículo antes de submeter a validação.
 */
@RestController
@RequestMapping("/api/leitores")
public class LeitorController {

    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<LeitorResponse> detalhe(@PathVariable String codigo) {
        return ResponseEntity.ok(leitorService.detalhe(codigo));
    }

    /** QR afixável no veículo (imagem PNG) que codifica o código do leitor. */
    @GetMapping(value = "/{codigo}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable String codigo) {
        byte[] png = Base64.getDecoder().decode(leitorService.gerarQrBase64(codigo));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }
}
