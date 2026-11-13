package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** A rede na emergência (E4b, ADR-0194): onde tem o recurso, os compromissos nas unidades afetadas e as regulações priorizadas. */
public final class RedeEmergenciaDtos {

    private RedeEmergenciaDtos() {
    }

    /** O saldo dentro da validade do medicamento ou do material numa unidade; no material, o livre já desconta a reserva. */
    public record Disponibilidade(UUID unidadeId, String unidadeNome, SituacaoOperacional situacaoOperacional, boolean afetada, int saldo,
                                  int reservado, int livre, LocalDate proximaValidade) implements Serializable {
    }

    /** Um agendamento ou uma solicitação regulada numa unidade afetada fora de operação, para o dono redirecionar. */
    public record Compromisso(String tipo, UUID id, UUID pacienteId, String pacienteNome, UUID unidadeId, String unidadeNome, LocalDateTime dataHora,
                              String descricao, String situacao) implements Serializable {
    }

    public record Regulacao(UUID id, UUID pacienteId, String pacienteNome, String procedimentoNome, PrioridadeRegulacao prioridade, String status,
                            String unidadeSolicitanteNome, String unidadeExecutanteNome, LocalDateTime dataHora, boolean priorizada) implements Serializable {
    }

    public record Rede(List<Compromisso> compromissos, List<Regulacao> priorizadas) implements Serializable {
    }
}
