package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.EtapaAcompanhamentoSorologico;
import com.edufelizardo.maissaudepublica.models.enuns.NexoTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoOcupacional;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoOcupacional;
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
                           List<Acompanhamento> acompanhamentos, Instant atualizadoEm, Investigacao investigacao) implements Serializable {
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
                         List<AcompanhamentoPendente> acompanhamentos, List<MedidaVencida> medidasVencidas) implements Serializable {
    }

    // ── V5b: investigação do trabalho (ADR-0208) ──

    /** A investigação do ambiente de trabalho, com as medidas, os riscos encontrados e os encaminhamentos. */
    public record Investigacao(UUID uuid, LocalDate visitaEm, String localVisitado, String achados, NexoTrabalho nexo, String justificativaNexo,
                               Instant nexoConcluidoEm, String investigadorNome, String denunciaProtocolo, SituacaoDenuncia denunciaSituacao,
                               List<Medida> medidas, List<Risco> riscos, List<Encaminhamento> encaminhamentos) implements Serializable {
    }

    /** Uma medida recomendada; vencida = recomendada com o prazo passado. */
    public record Medida(UUID uuid, TipoMedidaTrabalho tipo, String descricao, LocalDate prazo, SituacaoMedidaTrabalho situacao, boolean vencida,
                         LocalDate verificadaEm, String observacao) implements Serializable {
    }

    /** Um risco ocupacional. */
    public record Risco(UUID uuid, TipoRiscoOcupacional tipo, String agente, String descricao, String empregadorDocumento, String empregadorNome,
                        UUID estabelecimentoId, String local, Integer trabalhadoresExpostos, SituacaoRiscoOcupacional situacao, Instant controladoEm,
                        String motivoControle, UUID notificacaoId) implements Serializable {
    }

    /** Um encaminhamento intersetorial da investigação. */
    public record Encaminhamento(UUID uuid, String instituicaoNome, String motivo, LocalDate enviadoEm, LocalDate retornoEsperadoEm, String situacao)
            implements Serializable {
    }

    /** A medida vencida, no painel. */
    public record MedidaVencida(UUID notificacaoId, String pacienteNome, String unidadeNome, TipoMedidaTrabalho tipo, String descricao, LocalDate prazo)
            implements Serializable {
    }

    /** O acidente grave ou fatal da semana, na Sala de Situação: só o agravo, a unidade, a data e o empregador. */
    public record AlertaSemana(String agravoNome, String unidadeNome, LocalDate dataEvento, boolean obito, String empregadorNome) implements Serializable {
    }

    /** A semana da saúde do trabalhador, para a Sala de Situação. */
    public record Semana(long acidentes, long obitos, long investigacoesAbertas, long medidasVencidas, long riscosAtivos, List<AlertaSemana> alertas)
            implements Serializable {
    }

    /** Uma contagem por rótulo (seção do CNAE, grande grupo da CBO). */
    public record Contagem(String rotulo, long total) implements Serializable {
    }

    /** Uma linha por agravo no relatório. */
    public record LinhaAgravo(String agravo, long notificadas, long nexoConfirmado, long obitos, long menores) implements Serializable {
    }

    /** Os números da saúde do trabalhador no período, para o relatório Vigilância do #19. Sem as de nexo descartado. */
    public record Resumo(long total, long nexoConcluido, long nexoConfirmado, long nexoDescartado, long obitos, long menores, long biologicoSaude,
                         long medidasVencidas, List<LinhaAgravo> porAgravo, List<Contagem> porCnae, List<Contagem> porOcupacao) implements Serializable {
    }

    /** O acidente do servidor no RH sem notificação na vigilância; o paciente de mesmo CPF, se houver. */
    public record AcidenteSemNotificacao(UUID acidenteId, String profissionalMatricula, String profissionalNome, LocalDateTime dataHora,
                                         String descricao, boolean catEmitida, Integer diasAfastamento, UUID pacienteId)
            implements Serializable {
    }
}
