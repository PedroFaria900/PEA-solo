package pt.uminho.mei.bilhetica.service.validacao;

import pt.uminho.mei.bilhetica.entity.leitor.Leitor;
import pt.uminho.mei.bilhetica.entity.titulo.TituloTransporte;

import java.time.LocalDateTime;

/**
 * Dados imutáveis de uma tentativa de validação, partilhados por todas as regras.
 */
public record ContextoValidacao(
    TituloTransporte titulo,
    Leitor leitor,
    LocalDateTime agora,
    String email
) {}
