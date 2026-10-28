package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A vacinação na Sala de Situação (ADR-0149): as doses aplicadas no período, a cobertura das vacinas de referência hoje,
 * a homogeneidade, o abandono e as campanhas de vacinação do período com o andamento. Só números.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VacinacaoNaRedeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String abrangencia;
    private LocalDate de;
    private LocalDate ate;
    private long dosesAplicadas;
    private List<ContagemProducaoDto> porEstrategia;
    private List<ContagemProducaoDto> porVacina;
    private Double homogeneidade;
    private int vacinasNaMeta;
    private int vacinasAvaliadas;
    private int abandonoAlto;
    private List<CoberturaVacinalDto.Vacina> cobertura;
    private List<Campanha> campanhas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Campanha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
        private LocalDate inicio;
        private LocalDate fim;
        private SituacaoCampanha situacao;
        private Integer metaQuantidade;
        private int realizado;
    }
}
