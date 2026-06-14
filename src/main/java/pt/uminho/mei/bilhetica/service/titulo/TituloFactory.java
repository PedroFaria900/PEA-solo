package pt.uminho.mei.bilhetica.service.titulo;

import pt.uminho.mei.bilhetica.dto.ComprarTituloRequest;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;
import pt.uminho.mei.bilhetica.enums.TipoTitulo;

import java.math.BigDecimal;

/**
 * Estratégia de criação e tarifação por tipo de título.
 *
 * <p>Adicionar um novo tipo de título resume-se a criar uma nova implementação
 * desta interface (mais a subclasse de {@code TituloTransporte} e a constante em
 * {@code TipoTitulo}); {@code TituloService} descobre os beans automaticamente,
 * sem necessitar de qualquer edição.</p>
 */
public interface TituloFactory {

    /** Tipo de título tratado por esta fábrica. */
    TipoTitulo tipo();

    /** Calcula o preço total a debitar, consultando o tarifário. */
    BigDecimal calcularPreco(ComprarTituloRequest req, Utente utente);

    /** Constrói o título (estado ATIVO, associado ao utente) pronto a persistir. */
    TituloTransporte criar(ComprarTituloRequest req, Utente utente);
}
