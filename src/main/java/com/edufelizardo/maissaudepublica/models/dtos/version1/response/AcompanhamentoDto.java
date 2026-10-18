package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Um acompanhamento programático (ADR-0119). {@code atrasado}: sem atendimento na unidade responsável há mais tempo que a
 * periodicidade do programa — é só sugestão, não muda a situação. {@code avisoPublico}: por que o paciente está fora do
 * público-alvo, quando estiver.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private Integer pacienteIdade;
    private UUID programaId;
    private String programaCodigo;
    private String programaNome;
    private boolean sensivel;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID equipeId;
    private String equipeNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDate inicio;
    private LocalDate fim;
    private SituacaoAcompanhamento situacao;
    private MotivoSaidaAcompanhamento motivoSaida;
    private String observacaoSaida;
    private LocalDate ultimoAtendimento;
    private boolean atrasado;
    private String avisoPublico;
    private List<Evento> eventos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Evento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Instant ocorridoEm;
        private SituacaoAcompanhamento situacaoAnterior;
        private SituacaoAcompanhamento situacao;
        private String motivo;
        private String registradoPorCpf;
    }
}
