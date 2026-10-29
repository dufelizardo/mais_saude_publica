package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoContingencia;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O plano de contingência numa versão (ADR-0157). Na lista, só os níveis; critérios, ações, recursos e contatos vão vazios. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoContingenciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private int versao;
    private String nome;
    private UUID tipoId;
    private String tipoNome;
    private String objetivo;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private SituacaoPlanoContingencia situacao;
    private List<Nivel> niveis;
    private List<Unidade> unidades;
    private List<Criterio> criterios;
    private List<Acao> acoes;
    private List<Recurso> recursos;
    private List<Contato> contatos;
    private Instant criadoEm;
    private Instant publicadoEm;
    private Instant encerradoEm;
    private String motivoRevogacao;
    private List<Versao> versoes;

    /** Nível coberto. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Nivel implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private int ordem;
        private String nome;
    }

    /** Unidade envolvida. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Unidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
    }

    /** Critério de ativação. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Criterio implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID nivelId;
        private String nivelNome;
        private String descricao;
    }

    /** Ação prevista. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Acao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID nivelId;
        private String nivelNome;
        private String descricao;
        private String responsavel;
        private Integer prazoHoras;
        private UUID unidadeId;
        private String unidadeNome;
    }

    /** Recurso necessário. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Recurso implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String descricao;
        private Integer quantidade;
    }

    /** Contato. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Contato implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String nome;
        private String papel;
        private String telefone;
    }

    /** Uma versão do mesmo código. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Versao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private int versao;
        private SituacaoPlanoContingencia situacao;
        private Instant publicadoEm;
        private Instant encerradoEm;
    }
}
