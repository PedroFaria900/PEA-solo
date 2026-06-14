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
            t -> t.getTipoTitulo().name().toLowerCase(),
            Collectors.mapping(t -> Map.of(
                "id", t.getId(),
                "nome", t.getTipoTitulo() + " " + t.getPerfilUtente(),
                "preco", t.getPreco(),
                "zona", t.getZona() != null ? t.getZona().getNome() : "Sem Zona"
            ), Collectors.toList())
        ));

        return ResponseEntity.ok(catalogo);
    }
}
