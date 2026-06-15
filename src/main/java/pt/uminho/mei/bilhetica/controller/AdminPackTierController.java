package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.service.PackTierService;

import java.util.List;
import java.util.Map;

/**
 * Gestão de tiers de pack (admin).
 * Protegido por ROLE_ADMIN via SecurityConfig → /api/admin/** hasRole("ADMIN").
 * Leitura pública disponível em GET /api/catalogo/pack-tiers.
 */
@RestController
@RequestMapping("/api/admin/pack-tiers")
public class AdminPackTierController {

    private final PackTierService packTierService;

    public AdminPackTierController(PackTierService packTierService) {
        this.packTierService = packTierService;
    }

    /** Lista todos os tiers em ordem crescente. */
    @GetMapping
    public ResponseEntity<List<Integer>> listar() {
        return ResponseEntity.ok(packTierService.listar());
    }

    /** Acrescenta um novo tier. Corpo: { "viagens": N } */
    @PostMapping
    public ResponseEntity<String> adicionar(@RequestBody Map<String, Integer> body) {
        Integer viagens = body.get("viagens");
        if (viagens == null) {
            throw new RuntimeException("Campo 'viagens' obrigatório");
        }
        packTierService.adicionar(viagens);
        return ResponseEntity.ok("Tier " + viagens + " adicionado");
    }

    /** Remove um tier pelo número de viagens. */
    @DeleteMapping("/{viagens}")
    public ResponseEntity<String> remover(@PathVariable int viagens) {
        packTierService.remover(viagens);
        return ResponseEntity.ok("Tier " + viagens + " removido");
    }
}
