package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PapelTitularidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import com.edufelizardo.maissaudepublica.models.enuns.TemaGestao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAreaGestao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Área da estrutura de gestão (ADR-0175), com quem responde hoje, o histórico e as unidades da regional. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AreaGestaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoAreaGestao tipo;
    private TemaGestao tema;
    private String nome;
    private String sigla;
    private String descricao;
    private UUID superiorId;
    private String superiorNome;
    /** A regional da coordenação; na supervisão, a da coordenação a que está ligada. */
    private UUID regionalId;
    private String regionalNome;
    private boolean ativa;
    private Titularidade titular;
    private Titularidade substituto;
    private int unidades;
    /** Só no detalhe. */
    private List<Titularidade> historico;
    /** Só no detalhe: as unidades assistenciais abaixo da regional. */
    private List<Unidade> unidadesDaRegional;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Titularidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID profissionalId;
        private String profissionalNome;
        private String matricula;
        private PapelTitularidade papel;
        private LocalDate inicio;
        private LocalDate fim;
        private String ato;
    }

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
        private TipoUnidadeDeSaude tipo;
        private SituacaoOperacional situacaoOperacional;
        private boolean ativo;
    }
}
