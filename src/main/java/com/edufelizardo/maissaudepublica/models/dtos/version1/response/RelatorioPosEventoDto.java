package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRelatorioPosEvento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** O relatório pós-evento (ADR-0159): sem rascunho, uuid e situação nulos; os indicadores são sempre calculados. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RelatorioPosEventoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID emergenciaId;
    private SituacaoRelatorioPosEvento situacao;
    private String resumo;
    private String problemas;
    private String licoes;
    private List<Recomendacao> recomendacoes;
    private List<PlanoUsado> planosUsados;
    private Indicadores indicadores;
    private Instant criadoEm;
    private Instant atualizadoEm;
    private Instant finalizadoEm;

    /** Uma recomendação, com a revisão do plano que ela gerou. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Recomendacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String descricao;
        private String planoCodigo;
        private UUID planoRevisaoId;
        private Integer planoRevisaoVersao;
    }

    /** Um plano ativado na emergência. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class PlanoUsado implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String codigo;
        private String nome;
        private int versao;
    }

    /** Uma contagem por categoria. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Contagem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String chave;
        private int quantidade;
    }

    /** Os números do evento, calculados do que foi registrado. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Indicadores implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private double duracaoHoras;
        private double horasAteAtivacao;
        private String nivelMaximo;
        private int mudancasDeNivel;
        private int acoesTotal;
        private int acoesConcluidas;
        private int acoesCanceladas;
        private int acoesComPrazo;
        private int acoesConcluidasNoPrazo;
        private Double percentualNoPrazo;
        private int mobilizacoes;
        private int profissionaisMobilizados;
        private List<Contagem> mobilizacoesPorTipo;
        private int capacidadeAtivada;
        private Integer populacaoAfetada;
        private int unidadesAfetadas;
        private Integer leitosIndisponiveis;
        /** As estruturas temporárias usadas e a soma dos boletins; no evento de massa, o público estimado (ADR-0160). */
        private int estruturas;
        private int atendimentosEstruturas;
        private int remocoesEstruturas;
        private int casosGravesEstruturas;
        private int obitosEstruturas;
        private Integer publicoEstimado;
        /** Na epidemia, as notificações do agravo; no desastre ambiental, as exposições e a maior população exposta (ADR-0161). */
        private Integer casosNotificados;
        private Integer casosConfirmados;
        private Integer obitosAgravo;
        private int exposicoes;
        private Integer populacaoExpostaMaxima;
    }
}
