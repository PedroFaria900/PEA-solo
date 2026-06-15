package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.repository.TarifarioRepository;
import pt.uminho.mei.bilhetica.entity.Tarifario;
import pt.uminho.mei.bilhetica.service.PackTierService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final TarifarioRepository tarifarioRepository;
    private final PackTierService packTierService;

    public CatalogoController(TarifarioRepository tarifarioRepository, PackTierService packTierService) {
        this.tarifarioRepository = tarifarioRepository;
        this.packTierService = packTierService;
    }

    @GetMapping("/titulos")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getCatalogo() {
        List<Tarifario> tarifarios = tarifarioRepository.findAll();
        
        // Group by tipo_titulo
        Map<String, List<Map<String, Object>>> catalogo = tarifarios.stream().collect(Collectors.groupingBy(
            t -> t.getTipoTitulo().name().toLowerCase(),
            Collectors.mapping(t -> Map.of(
                "id", t.getId(),
                "nome", t.getTipoTitulo() + " " + t.getPerfilUtente(),
                "preco", t.getPreco(),
                "zona", t.getZona() != null ? t.getZona().getNome() : "Rede completa"
            ), Collectors.toList())
        ));

        return ResponseEntity.ok(catalogo);
    }

    /** Tiers de viagens válidos para compra de um pack (geridos em base de dados via /api/admin/pack-tiers). */
    @GetMapping("/pack-tiers")
    public ResponseEntity<List<Integer>> getPackTiers() {
        return ResponseEntity.ok(packTierService.listar());
    }
}
