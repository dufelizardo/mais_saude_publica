package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CausalidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRemanejamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRemanejamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSuspensaoLote;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O remanejamento entre salas de vacina e o lote na rede: o alerta de ESAVI e a suspensão (ADR-0206). */
public final class ImunizacaoRedeDtos {

    private ImunizacaoRedeDtos() {
    }

    public record Remanejamento(UUID uuid, UUID loteOrigemId, UUID unidadeOrigemId, String unidadeOrigemNome, UUID unidadeDestinoId,
                                String unidadeDestinoNome, UUID imunobiologicoId, String sigla, String numeroLote, LocalDate validade, int doses,
                                MotivoRemanejamento motivo, String observacao, SituacaoRemanejamento situacao, Instant enviadoEm, Instant concluidoEm,
                                BigDecimal temperaturaChegada, boolean chegouForaDaFaixa, String motivoRecusa, UUID loteDestinoId) {
    }

    /** A notificação de ESAVI do sinal, sem o paciente. */
    public record NotificacaoDoLote(UUID uuid, LocalDate dataNotificacao, String unidadeNome, GravidadeEsavi gravidade, CausalidadeEsavi causalidade) {
    }

    public record Suspensao(UUID uuid, UUID imunobiologicoId, String sigla, String numeroLote, String sinal, String motivo, SituacaoSuspensaoLote situacao,
                            Instant suspensaEm, Instant decididaEm, String parecer, int lotesAlcancados) {
    }

    /** O lote com sinal de ESAVI (ou suspenso pela rede), com o estoque na rede e a última suspensão. */
    public record AlertaLote(UUID imunobiologicoId, String sigla, String imunobiologicoNome, String numeroLote, long gravesEm30Dias, boolean desvioDeQualidade,
                             String sinal, List<NotificacaoDoLote> notificacoes, int salasComEstoque, int dosesEmEstoque, Suspensao suspensao) {
    }
}
