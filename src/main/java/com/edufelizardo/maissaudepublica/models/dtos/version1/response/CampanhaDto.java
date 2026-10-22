package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampanha;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma campanha (ADR-0133), com os locais e o andamento da meta. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private String descricao;
    private TipoCampanha tipo;
    private UUID programaId;
    private String programaNome;
    private LocalDate inicio;
    private LocalDate fim;
    private SituacaoCampanha situacao;
    /** O público em texto, montado dos campos (ou a descrição, quando houver). */
    private String publico;
    private Integer idadeMinima;
    private Integer idadeMaxima;
    private Sexo sexo;
    private boolean gestante;
    private List<String> condicoes;
    private String publicoDescricao;
    private String metaDescricao;
    private Integer metaQuantidade;
    /** Soma do atendido nas ações realizadas. */
    private int realizado;
    private boolean publicada;
    private Instant canceladaEm;
    private String motivoCancelamento;
    private int acoesPendentes;
    private int acoesRealizadas;
    private List<Local> locais;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Local implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private boolean temporario;
        private UUID unidadeId;
        private String unidadeNome;
        /** O nome a mostrar: o do local temporário, ou o da unidade. */
        private String nome;
        private String endereco;
        private Double latitude;
        private Double longitude;
        private String funcionamento;
        private LocalDate inicio;
        private LocalDate fim;
    }
}
