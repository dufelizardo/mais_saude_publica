package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoAlvoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoComunicado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Campanha de comunicação (ADR-0174), com os conteúdos em vigor, os comunicados e o alcance por canal. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaComunicacaoDto implements Serializable {
    /** C3 (ADR-0195): as ações educativas de Programas ligadas à campanha. */
    private List<AcaoEducativaDto.DaCampanha> acoesEducativas;

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private String objetivo;
    private String descricao;
    private LocalDate inicio;
    private LocalDate fim;
    private SituacaoCampanhaComunicacao situacao;
    private PublicoAlvoCampanha publicoAlvo;
    private String faixaEtaria;
    private UUID unidadeId;
    private String unidadeNome;
    private List<Item> territorios;
    private List<String> temas;
    private List<UUID> canalIds;
    private List<EducacaoPermanenteDto.Conteudo> conteudos;
    private UUID programaId;
    private String programaNome;
    private UUID campanhaOperacionalId;
    private String campanhaOperacionalNome;
    private List<ComunicadoDaCampanha> comunicados;
    private List<Alcance> alcance;
    private LocalDateTime iniciadaEm;
    private LocalDateTime encerradaEm;
    private String balanco;
    private String motivoCancelamento;

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
        private String nome;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class ComunicadoDaCampanha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String titulo;
        private SituacaoComunicado situacao;
        private LocalDate inicio;
        private LocalDate fim;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Alcance implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String canal;
        private int publicacoes;
        private int enviados;
        private int falhas;
    }
}
