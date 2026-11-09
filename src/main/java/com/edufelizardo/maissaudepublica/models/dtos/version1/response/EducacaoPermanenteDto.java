package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Respostas da educação permanente (ADR-0173). */
public final class EducacaoPermanenteDto {

    private EducacaoPermanenteDto() {
    }

    /** Competência do catálogo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Competencia implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String codigo;
        private String nome;
        private String descricao;
        private boolean ativo;
    }

    /** Conteúdo na versão em vigor; vencido quando a validade passou. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Conteudo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID grupoId;
        private UUID uuid;
        private String titulo;
        private int versao;
        private SituacaoConteudo situacao;
        private PublicoComunicacao publico;
        private LocalDate validade;
        private boolean vencido;
    }

    /** As competências e os conteúdos de um treinamento. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Treinamento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID treinamentoId;
        private String treinamentoNome;
        private List<Competencia> competencias;
        private List<Conteudo> conteudos;
    }

    /** Competência desenvolvida pelo profissional, a partir da participação concluída. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class CompetenciaDoProfissional implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID competenciaId;
        private String codigo;
        private String nome;
        private String treinamentoNome;
        private LocalDate dataConclusao;
        private LocalDate dataValidade;
        private boolean vencida;
    }

    /** Em quais treinamentos o conteúdo é usado. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class UsoDoConteudo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID grupoId;
        private List<String> treinamentos;
    }
}
