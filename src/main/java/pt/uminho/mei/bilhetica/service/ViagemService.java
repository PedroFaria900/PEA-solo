package pt.uminho.mei.bilhetica.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.ViagemResponse;
import pt.uminho.mei.bilhetica.entity.Viagem;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;
import pt.uminho.mei.bilhetica.repository.ViagemRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final UtenteRepository utenteRepository;

    public ViagemService(ViagemRepository viagemRepository,
                         UtenteRepository utenteRepository) {
        this.viagemRepository = viagemRepository;
        this.utenteRepository = utenteRepository;
    }

    public List<ViagemResponse> historico(String email, Pageable pageable) {
        var utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        return viagemRepository.findByUtenteId(utente.getId(), pageable)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ViagemResponse detalhe(UUID id, String email) {
        Viagem viagem = viagemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));
        String owner = viagem.getValidacao().getTitulo().getUtente().getEmail();
        if (!owner.equals(email)) {
            throw new RuntimeException("Viagem não encontrada");
        }
        return toResponse(viagem);
    }

    private ViagemResponse toResponse(Viagem v) {
        String linha = null;
        if (v.getValidacao() != null
                && v.getValidacao().getLeitor() != null
                && v.getValidacao().getLeitor().getLinha() != null) {
            linha = v.getValidacao().getLeitor().getLinha().getDesignacao();
        }

        return ViagemResponse.builder()
            .id(v.getId())
            .momento(v.getMomento())
            .linha(linha)
            .build();
    }
}
