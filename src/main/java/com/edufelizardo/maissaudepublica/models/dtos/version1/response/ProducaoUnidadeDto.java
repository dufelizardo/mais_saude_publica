package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Uma unidade no ranking de produção (ADR-0143): concluídos no período e no anterior. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProducaoUnidadeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String unidadeNome;
    private TipoUnidadeDeSaude tipo;
    private long atendimentos;
    private long atendimentosAnterior;
    /** Em %; nula quando o anterior é zero. */
    private BigDecimal variacao;
}
