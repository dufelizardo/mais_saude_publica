package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** Um relatório pronto para a tela e para o CSV (ADR-0167): os números já vêm como texto, no formato brasileiro. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RelatorioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nome;
    private String categoria;
    private LocalDate de;
    private LocalDate ate;
    private String abrangencia;
    private List<Indicador> resumo;
    private List<Secao> secoes;
    private List<String> notas;

    /** Um número de destaque. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Indicador implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String rotulo;
        private String valor;
    }

    /** Uma tabela. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Secao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String titulo;
        private List<String> colunas;
        private List<List<String>> linhas;
    }
}
