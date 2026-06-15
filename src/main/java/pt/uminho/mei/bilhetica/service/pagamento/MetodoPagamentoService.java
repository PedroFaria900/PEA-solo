package pt.uminho.mei.bilhetica.service.pagamento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.MetodoRequest;
import pt.uminho.mei.bilhetica.dto.MetodoResponse;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoPagamento;
import pt.uminho.mei.bilhetica.enums.TipoMetodoPagamento;
import pt.uminho.mei.bilhetica.repository.MetodoPagamentoRepository;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MetodoPagamentoService {

    private final MetodoPagamentoRepository metodoRepository;
    private final UtenteRepository utenteRepository;
    private final Map<TipoMetodoPagamento, MetodoPagamentoFactory> factories;

    public MetodoPagamentoService(MetodoPagamentoRepository metodoRepository,
                                   UtenteRepository utenteRepository,
                                   List<MetodoPagamentoFactory> factoryList) {
        this.metodoRepository = metodoRepository;
        this.utenteRepository = utenteRepository;
        this.factories = factoryList.stream()
            .collect(Collectors.toMap(MetodoPagamentoFactory::tipo, Function.identity()));
    }

    public List<MetodoResponse> listar(String email) {
        Utente utente = resolveUtente(email);
        return metodoRepository.findByUtenteId(utente.getId())
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MetodoResponse adicionar(String email, MetodoRequest req) {
        if (req.getTipo() == null) {
            throw new RuntimeException("Tipo de método obrigatório");
        }
        MetodoPagamentoFactory factory = factories.get(req.getTipo());
        if (factory == null) {
            throw new RuntimeException("Tipo de método inválido");
        }
        Utente utente = resolveUtente(email);
        MetodoPagamento metodo = factory.criar(req, utente);
        // primeiro método adicionado torna-se automaticamente predefinido
        metodo.setPredefinido(metodoRepository.countByUtenteId(utente.getId()) == 0);
        return toResponse(metodoRepository.save(metodo));
    }

    @Transactional
    public MetodoResponse editar(String email, UUID id, MetodoRequest req) {
        Utente utente = resolveUtente(email);
        MetodoPagamento metodo = findForUtente(id, utente.getId());
        // dispatch pelo tipo actual do método — sem instanceof no serviço
        factories.get(metodo.tipo()).atualizar(metodo, req);
        return toResponse(metodoRepository.save(metodo));
    }

    @Transactional
    public void remover(String email, UUID id) {
        Utente utente = resolveUtente(email);
        MetodoPagamento metodo = findForUtente(id, utente.getId());
        boolean eraPredefinido = metodo.isPredefinido();
        metodoRepository.delete(metodo);
        if (eraPredefinido) {
            // promover outro método (o primeiro restante), se existir
            List<MetodoPagamento> restantes = metodoRepository.findByUtenteId(utente.getId());
            if (!restantes.isEmpty()) {
                restantes.get(0).setPredefinido(true);
                metodoRepository.save(restantes.get(0));
            }
        }
    }

    @Transactional
    public MetodoResponse definirPredefinido(String email, UUID id) {
        Utente utente = resolveUtente(email);
        MetodoPagamento novo = findForUtente(id, utente.getId());
        // desmarcar o predefinido actual (se existir e for diferente)
        metodoRepository.findByUtenteIdAndPredefinidoTrue(utente.getId()).ifPresent(atual -> {
            if (!atual.getId().equals(novo.getId())) {
                atual.setPredefinido(false);
                metodoRepository.save(atual);
            }
        });
        novo.setPredefinido(true);
        return toResponse(metodoRepository.save(novo));
    }

    private MetodoResponse toResponse(MetodoPagamento m) {
        return MetodoResponse.builder()
            .id(m.getId())
            .tipo(m.tipo())
            .predefinido(m.isPredefinido())
            .resumo(m.resumo())
            .build();
    }

    private Utente resolveUtente(String email) {
        return utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
    }

    /**
     * Carrega o método e verifica que pertence ao utente dado.
     * Lança "não encontrado" em ambos os casos (não revela existência).
     */
    private MetodoPagamento findForUtente(UUID metodoId, UUID utenteId) {
        MetodoPagamento m = metodoRepository.findById(metodoId)
            .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));
        if (!m.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Método de pagamento não encontrado");
        }
        return m;
    }
}
