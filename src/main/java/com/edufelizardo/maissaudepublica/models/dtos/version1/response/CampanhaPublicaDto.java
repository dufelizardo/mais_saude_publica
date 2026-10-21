package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampanha;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** Campanha na consulta pública (ADR-0133): só o que serve ao cidadão. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaPublicaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private TipoCampanha tipo;
    private String descricao;
    private String publico;
    private LocalDate inicio;
    private LocalDate fim;
    private SituacaoCampanha situacao;
    private String meta;
    private Integer metaQuantidade;
    private int realizado;
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

        private String nome;
        private String endereco;
        private String funcionamento;
        private Double latitude;
        private Double longitude;
    }
}
