package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Emissões do laudo em PDF e a verificação pública pelo código (ADR-0183). */
public final class EmissaoLaudoDtos {

    private EmissaoLaudoDtos() {
    }

    /** Uma emissão do laudo do pedido; a vigente é a de versão mais alta. */
    public record Emissao(UUID uuid, int versao, String codigo, String hashPdf, Instant emitidoEm, int exames, boolean vigente) {
    }

    /**
     * O que a verificação pública mostra, sem login: a validade, a versão, a unidade, os exames e o hash. Do paciente, só as
     * iniciais (LGPD).
     */
    public record Verificacao(String codigo, boolean valido, boolean substituido, int versao, int versaoVigente, Instant emitidoEm,
                              String unidadeSolicitanteNome, String pacienteIniciais, List<String> exames, String hashPdf) {
    }
}
