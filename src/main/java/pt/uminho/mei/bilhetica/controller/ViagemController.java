package pt.uminho.mei.bilhetica.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.ViagemResponse;
import pt.uminho.mei.bilhetica.service.ViagemService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagens")
public class ViagemController {

    private final ViagemService viagemService;

    public ViagemController(ViagemService viagemService) {
        this.viagemService = viagemService;
    }

    // Default: newest 20. Callers may pass ?page=&size= to navigate further.
    // Response stays a plain JSON array for client compatibility.
    @GetMapping
    public ResponseEntity<List<ViagemResponse>> historico(
            @AuthenticationPrincipal UserDetails user,
            @PageableDefault(size = 20, sort = "momento",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
            viagemService.historico(user.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemResponse> detalhe(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        return ResponseEntity.ok(viagemService.detalhe(id, user.getUsername()));
    }
}
