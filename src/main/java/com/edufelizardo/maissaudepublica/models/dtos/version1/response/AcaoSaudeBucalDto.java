package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AtividadeSaudeBucalColetiva;
import com.edufelizardo.maissaudepublica.models.enuns.LocalSaudeBucalColetiva;
import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** O detalhe odontológico da ação coletiva e as avaliações dos participantes (ADR-0140). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoSaudeBucalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID acaoId;
    /** Falso enquanto o detalhe não foi preenchido. */
    private boolean preenchido;
    private List<AtividadeSaudeBucalColetiva> atividades;
    private LocalSaudeBucalColetiva localTipo;
    private String instituicao;
    private String publicoAlvo;
    private Integer kitsDistribuidos;
    private Instant atualizadoEm;
    private List<Avaliacao> avaliacoes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Avaliacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID pacienteId;
        private String pacienteNome;
        private RiscoSaudeBucal risco;
        private NecessidadeTratamentoOdontologico necessidadeTratamento;
        private String observacao;
        private Instant registradoEm;
    }
}
