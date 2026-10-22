package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoItemPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** O plano de tratamento com os itens (ADR-0137). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoTratamentoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private UUID atendimentoId;
    private UUID avaliacaoId;
    private String unidadeNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private String diagnostico;
    private String objetivo;
    private NecessidadeTratamentoOdontologico prioridade;
    private SituacaoPlanoTratamento situacao;
    private Instant abertoEm;
    /** Na conclusão, a data do tratamento concluído. */
    private Instant encerradoEm;
    private String motivoEncerramento;
    private List<Item> itens;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private int ordem;
        private UUID procedimentoCatalogoId;
        private String procedimentoNome;
        private String sigtap;
        private AlcanceProcedimento alcance;
        private boolean exigeDentista;
        private Integer dente;
        private List<FaceDente> faces;
        private String observacao;
        private SituacaoItemPlanoTratamento situacao;
        private UUID procedimentoId;
        private StatusProcedimento procedimentoStatus;
        private UUID solicitacaoRegulacaoId;
        private String motivoSituacao;
        private Instant situacaoAlteradaEm;
        /** O encaminhamento pela Regulação (ADR-0138): o item, a situação, a executante e a contrarreferência. */
        private String regulacaoProcedimentoNome;
        private StatusSolicitacaoRegulacao regulacaoStatus;
        private String regulacaoExecutanteNome;
        private String contrarreferencia;
    }
}
