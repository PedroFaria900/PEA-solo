package pt.uminho.mei.bilhetica.service;

import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.LeitorResponse;
import pt.uminho.mei.bilhetica.entity.leitor.Leitor;
import pt.uminho.mei.bilhetica.repository.LeitorRepository;
import pt.uminho.mei.bilhetica.security.QrCodeUtil;

@Service
public class LeitorService {

    private final LeitorRepository leitorRepository;
    private final QrCodeUtil qrCodeUtil;

    public LeitorService(LeitorRepository leitorRepository, QrCodeUtil qrCodeUtil) {
        this.leitorRepository = leitorRepository;
        this.qrCodeUtil = qrCodeUtil;
    }

    public LeitorResponse detalhe(String codigo) {
        Leitor leitor = obter(codigo);
        return LeitorResponse.builder()
            .id(leitor.getId())
            .codigo(leitor.getCodigo())
            .linhaDesignacao(leitor.getLinha() != null ? leitor.getLinha().getDesignacao() : null)
            .estado(leitor.getEstado())
            .build();
    }

    /** QR (PNG base64) a afixar no veículo; o seu conteúdo é o próprio código do leitor. */
    public String gerarQrBase64(String codigo) {
        Leitor leitor = obter(codigo);
        return qrCodeUtil.gerarQrBase64(leitor.getCodigo());
    }

    private Leitor obter(String codigo) {
        return leitorRepository.findByCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Leitor não encontrado: " + codigo));
    }
}
