package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEtapaPercurso;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEtapaLinhaCuidado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O percurso do paciente na linha (ADR-0152), com a situação de cada etapa calculada na hora. Dado de saúde. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PercursoLinhaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID linhaId;
    private String linhaCodigo;
    private String linhaNome;
    private boolean sensivel;
    private LocalDate inicio;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID equipeId;
    private String equipeNome;
    private UUID acompanhamentoId;
    private SituacaoPercursoLinha situacao;
    private LocalDate encerradoEm;
    private String motivoEncerramento;
    /** Alguma etapa obrigatória passou do prazo. */
    private boolean atrasado;
    /** A primeira obrigatória ainda não cumprida nem dispensada. */
    private String etapaAtual;
    private int obrigatoriasFeitas;
    private int obrigatorias;
    private Instant registradoEm;
    private List<Etapa> etapas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Etapa implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID etapaId;
        private int ordem;
        private String nome;
        private TipoEtapaLinhaCuidado tipo;
        private boolean obrigatoria;
        private int quantidade;
        private int realizadas;
        private SituacaoEtapaPercurso situacao;
        /** O limite: a referência mais o prazo; vazio sem prazo ou aguardando a anterior. */
        private LocalDate prazo;
        private LocalDate cumpridaEm;
        /** O que cumpriu ("Atendimento em 12/03/2026 · Enfermagem"), ou o andamento da regulação. */
        private String evidencia;
        /** O motivo da dispensa, ou a observação do registro manual. */
        private String observacao;
    }
}
