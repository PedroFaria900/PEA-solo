package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.repository.TarifarioRepository;
import pt.uminho.mei.bilhetica.entity.Tarifario;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final TarifarioRepository tarifarioRepository;

    public CatalogoController(TarifarioRepository tarifarioRepository) {
        this.tarifarioRepository = tarifarioRepository;
    }

    @GetMapping("/titulos")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getCatalogo() {
        List<Tarifario> tarifarios = tarifarioRepository.findAll();
        
        // Group by tipo_titulo
        Map<String, List<Map<String, Object>>> catalogo = tarifarios.stream().collect(Collectors.groupingBy(
            t -> t.getTipoTitulo().toLowerCase(),
            Collectors.mapping(t -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", t.getId());
                map.put("nome", t.getTipoTitulo() + " " + t.getPerfilUtente());
                map.put("preco", t.getPreco());
                map.put("tipo", t.getTipoTitulo());
                map.put("perfil", t.getPerfilUtente());
                map.put("zona", t.getZona() != null ? t.getZona().getNome() : "Sem Zona");
                map.put("zonaId", t.getZona() != null ? t.getZona().getId() : null);
                return map;
            }, Collectors.toList())
        ));

        return ResponseEntity.ok(catalogo);
    }
}
