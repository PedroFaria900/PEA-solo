package pt.uminho.mei.bilhetica.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.uminho.mei.bilhetica.dto.CarregamentoRequest;
import pt.uminho.mei.bilhetica.dto.SaldoResponse;
import pt.uminho.mei.bilhetica.dto.TransacaoResponse;
import pt.uminho.mei.bilhetica.entity.Transacao;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.pagamento.MetodoPagamento;
import pt.uminho.mei.bilhetica.enums.TipoTransacao;
import pt.uminho.mei.bilhetica.repository.MetodoPagamentoRepository;
import pt.uminho.mei.bilhetica.repository.TransacaoRepository;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarteiraService {

    private final UtenteRepository utenteRepository;
    private final TransacaoRepository transacaoRepository;
    private final MetodoPagamentoRepository metodoRepository;

    public CarteiraService(UtenteRepository utenteRepository,
                           TransacaoRepository transacaoRepository,
                           MetodoPagamentoRepository metodoRepository) {
        this.utenteRepository = utenteRepository;
        this.transacaoRepository = transacaoRepository;
        this.metodoRepository = metodoRepository;
    }

    public SaldoResponse obterSaldo(String email) {
        Utente utente = resolveUtente(email);
        return SaldoResponse.builder().saldo(utente.getSaldo()).build();
    }

    @Transactional
    public SaldoResponse carregar(String email, CarregamentoRequest req) {
        if (req.getValor() == null || req.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor inválido");
        }
        Utente utente = resolveUtente(email);

        // validar método indicado (se fornecido); carga sem método é permitida
        if (req.getMetodoId() != null) {
            MetodoPagamento metodo = metodoRepository.findById(req.getMetodoId())
                .orElseThrow(() -> new RuntimeException("Método de pagamento não encontrado"));
            if (!metodo.getUtente().getId().equals(utente.getId())) {
                throw new RuntimeException("Método de pagamento não encontrado");
            }
        }

        creditar(utente, req.getValor(), "Carregamento de saldo");
        return SaldoResponse.builder().saldo(utente.getSaldo()).build();
    }

    public List<TransacaoResponse> historico(String email, Pageable pageable) {
        Utente utente = resolveUtente(email);
        return transacaoRepository.findByUtenteId(utente.getId(), pageable)
            .stream()
            .map(t -> TransacaoResponse.builder()
                .id(t.getId())
                .valor(t.getValor())
                .tipo(t.getTipo())
                .momento(t.getMomento())
                .descricao(t.getDescricao())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Soma valor ao saldo e regista uma transação CARREGAMENTO (valor positivo).
     * Transaccional — participa na transação do chamador se existir.
     */
    @Transactional
    public Transacao creditar(Utente utente, BigDecimal valor, String descricao) {
        utente.setSaldo(utente.getSaldo().add(valor));
        utenteRepository.save(utente);
        return transacaoRepository.save(Transacao.builder()
            .utente(utente)
            .valor(valor)
            .tipo(TipoTransacao.CARREGAMENTO)
            .momento(LocalDateTime.now())
            .descricao(descricao)
            .build());
    }

    /**
     * Subtrai valor do saldo e regista uma transação COMPRA (valor negativo).
     * Valida saldo suficiente antes de debitar.
     * Transaccional — participa na transação do chamador (ex.: TituloService.comprar).
     */
    @Transactional
    public Transacao debitar(Utente utente, BigDecimal valor, String descricao) {
        if (utente.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente. Necessário: " + valor
                + ", disponível: " + utente.getSaldo());
        }
        utente.setSaldo(utente.getSaldo().subtract(valor));
        utenteRepository.save(utente);
        return transacaoRepository.save(Transacao.builder()
            .utente(utente)
            .valor(valor.negate())
            .tipo(TipoTransacao.COMPRA)
            .momento(LocalDateTime.now())
            .descricao(descricao)
            .build());
    }

    private Utente resolveUtente(String email) {
        return utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
    }
}
