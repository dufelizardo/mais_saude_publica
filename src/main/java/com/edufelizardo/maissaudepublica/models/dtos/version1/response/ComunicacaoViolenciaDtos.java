package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas da comunicação obrigatória da violência, fatia V3c (ADR-0201). */
public final class ComunicacaoViolenciaDtos {

    private ComunicacaoViolenciaDtos() {
    }

    /** situacao: NAO_SE_APLICA, PENDENTE, VENCIDA ou FEITA; noPrazo, quando feita. */
    public record Comunicacao(boolean obrigatoria, String destino, List<String> tiposAceitos, String baseLegal, Instant prazo, String situacao,
                              Instant feitaEm, Boolean noPrazo, List<EncaminhamentoIntersetorialDto> encaminhamentos) {
    }

    public record Pendente(UUID notificacaoId, String pacienteNome, String unidadeNome, LocalDate dataNotificacao, String destino, Instant prazo,
                           boolean vencida) {
    }

    public record Resumo(String destino, long obrigatorias, long feitas, long noPrazo) {
    }
}
