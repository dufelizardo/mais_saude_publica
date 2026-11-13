package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.EtapaAcompanhamentoSorologico;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaCat;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMercadoTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcidenteTrabalho;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Saúde do Trabalhador (ADR-0207). */
public final class SaudeTrabalhadorDtos {
    private SaudeTrabalhadorDtos() {
    }

    /** O bloco do trabalho de uma notificação. */
    public record Trabalho(UUID uuid, String ocupacaoCbo, String ocupacaoDescricao, SituacaoMercadoTrabalho situacaoTrabalho,
                           String empregadorDocumento, String empregadorNome, String empregadorCnae, UUID estabelecimentoId,
                           String localEvento, Double latitude, Double longitude, TipoAcidenteTrabalho tipoAcidente,
                           Integer horasAposInicioJornada, RespostaCat cat, Integer outrosTrabalhadoresAtingidos, boolean obito,
                           UUID acidenteTrabalhoId, LocalDateTime acidenteTrabalhoDataHora, boolean acidente, boolean materialBiologico,
                           List<Acompanhamento> acompanhamentos, Instant atualizadoEm) implements Serializable {
    }

    /** Uma etapa do acompanhamento sorológico: a data prevista e a realizada. */
    public record Acompanhamento(EtapaAcompanhamentoSorologico etapa, LocalDate prevista, LocalDate realizadoEm, boolean vencida)
            implements Serializable {
    }

    /** Uma notificação relacionada ao trabalho, no painel. */
    public record NotificacaoTrabalho(UUID notificacaoId, UUID pacienteId, String pacienteNome, String agravoCodigo, String agravoNome,
                                      String unidadeNome, LocalDate dataNotificacao, LocalDate dataEvento, SituacaoNotificacao situacao,
                                      String ocupacao, SituacaoMercadoTrabalho situacaoTrabalho, String empregadorNome, String empregadorCnae,
                                      boolean obito, boolean servidor) implements Serializable {
    }

    /** A etapa do acompanhamento sorológico a fazer (vencida ou nos próximos 7 dias). */
    public record AcompanhamentoPendente(UUID notificacaoId, String pacienteNome, String unidadeNome, EtapaAcompanhamentoSorologico etapa,
                                         LocalDate prevista, boolean vencida) implements Serializable {
    }

    /** O painel da saúde do trabalhador. */
    public record Painel(long total, long acidentes, long obitos, long materialBiologico, List<NotificacaoTrabalho> notificacoes,
                         List<AcompanhamentoPendente> acompanhamentos) implements Serializable {
    }

    /** O acidente do servidor no RH sem notificação na vigilância; o paciente de mesmo CPF, se houver. */
    public record AcidenteSemNotificacao(UUID acidenteId, String profissionalMatricula, String profissionalNome, LocalDateTime dataHora,
                                         String descricao, boolean catEmitida, Integer diasAfastamento, UUID pacienteId)
            implements Serializable {
    }
}
