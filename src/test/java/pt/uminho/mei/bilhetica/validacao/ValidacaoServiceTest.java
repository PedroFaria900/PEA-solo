package pt.uminho.mei.bilhetica.validacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.uminho.mei.bilhetica.dto.ValidacaoRequest;
import pt.uminho.mei.bilhetica.dto.ValidacaoResponse;
import pt.uminho.mei.bilhetica.entity.Validacao;
import pt.uminho.mei.bilhetica.entity.Viagem;
import pt.uminho.mei.bilhetica.entity.leitor.Leitor;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.EstadoLeitor;
import pt.uminho.mei.bilhetica.enums.ResultadoValidacao;
import pt.uminho.mei.bilhetica.repository.*;
import pt.uminho.mei.bilhetica.service.ValidacaoService;
import pt.uminho.mei.bilhetica.service.validacao.RegraValidacao;
import pt.uminho.mei.bilhetica.service.validacao.ResultadoRegra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Cobre a cadeia de regras e a resolução do leitor em {@link ValidacaoService}. */
class ValidacaoServiceTest {

    private ValidacaoRepository validacaoRepository;
    private ViagemRepository viagemRepository;
    private LeitorRepository leitorRepository;
    private TituloTransporteRepository tituloRepository;

    private TituloTransporte titulo;
    private final UUID tituloId = UUID.randomUUID();
    private final String leitorCodigo = "SER_52f1-BUS01";

    @BeforeEach
    void setup() {
        validacaoRepository = mock(ValidacaoRepository.class);
        viagemRepository = mock(ViagemRepository.class);
        leitorRepository = mock(LeitorRepository.class);
        tituloRepository = mock(TituloTransporteRepository.class);

        titulo = mock(TituloTransporte.class);
        when(tituloRepository.findById(tituloId)).thenReturn(Optional.of(titulo));
        when(validacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(viagemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private ValidacaoService service(List<RegraValidacao> regras) {
        return new ValidacaoService(validacaoRepository, viagemRepository,
            leitorRepository, tituloRepository, regras);
    }

    private void leitorAtivo() {
        Leitor leitor = new Leitor();
        leitor.setCodigo(leitorCodigo);
        leitor.setEstado(EstadoLeitor.ACTIVO);
        when(leitorRepository.findByCodigo(leitorCodigo)).thenReturn(Optional.of(leitor));
    }

    private ValidacaoRequest request() {
        return new ValidacaoRequest(tituloId, leitorCodigo);
    }

    @Test
    void leitorDesconhecido_naoPersisteNada() {
        when(leitorRepository.findByCodigo(leitorCodigo)).thenReturn(Optional.empty());

        ValidacaoResponse resp = service(List.of()).processar(request(), "ana@email.com");

        assertThat(resp.getResultado()).isEqualTo(ResultadoValidacao.INVALIDO);
        assertThat(resp.getMensagem()).isEqualTo("Leitor desconhecido");
        verify(validacaoRepository, never()).save(any());
    }

    @Test
    void leitorInactivo_registaInvalido() {
        Leitor leitor = new Leitor();
        leitor.setCodigo(leitorCodigo);
        leitor.setEstado(EstadoLeitor.AVARIA);
        when(leitorRepository.findByCodigo(leitorCodigo)).thenReturn(Optional.of(leitor));

        ValidacaoResponse resp = service(List.of()).processar(request(), "ana@email.com");

        assertThat(resp.getResultado()).isEqualTo(ResultadoValidacao.INVALIDO);
        assertThat(resp.getMensagem()).isEqualTo("Leitor inactivo");
        verify(validacaoRepository, times(1)).save(any(Validacao.class));
        verify(viagemRepository, never()).save(any());
    }

    @Test
    void regraQueFalha_interrompeSemConsumirNemRegistarViagem() {
        leitorAtivo();
        RegraValidacao reprova = ctx ->
            Optional.of(new ResultadoRegra(ResultadoValidacao.SEM_SALDO, "Pack sem viagens disponíveis"));

        ValidacaoResponse resp = service(List.of(reprova)).processar(request(), "ana@email.com");

        assertThat(resp.getResultado()).isEqualTo(ResultadoValidacao.SEM_SALDO);
        assertThat(resp.getMensagem()).isEqualTo("Pack sem viagens disponíveis");
        verify(titulo, never()).registarConsumo(any());
        verify(viagemRepository, never()).save(any());
        verify(validacaoRepository, times(1)).save(any(Validacao.class)); // grava a tentativa inválida
    }

    @Test
    void primeiraRegraFalha_naoAvaliaAsSeguintes() {
        leitorAtivo();
        RegraValidacao reprova = ctx ->
            Optional.of(new ResultadoRegra(ResultadoValidacao.INVALIDO, "Título não está activo"));
        RegraValidacao naoDeveCorrer = mock(RegraValidacao.class);

        service(List.of(reprova, naoDeveCorrer)).processar(request(), "ana@email.com");

        verify(naoDeveCorrer, never()).verificar(any());
    }

    @Test
    void todasAsRegrasPassam_validoEConsomeERegistaViagem() {
        leitorAtivo();

        ValidacaoResponse resp = service(List.of()).processar(request(), "ana@email.com");

        assertThat(resp.getResultado()).isEqualTo(ResultadoValidacao.VALIDO);
        assertThat(resp.getMensagem()).isEqualTo("Boa viagem!");
        verify(titulo, times(1)).registarConsumo(any());
        verify(tituloRepository, times(1)).save(titulo);
        verify(validacaoRepository, times(1)).save(any(Validacao.class));
        verify(viagemRepository, times(1)).save(any(Viagem.class));
    }
}
