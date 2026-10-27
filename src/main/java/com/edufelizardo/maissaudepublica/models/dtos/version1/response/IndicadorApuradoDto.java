package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMetaProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.TipoValorIndicador;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Um indicador programático apurado no período (ADR-0141). Só números: nunca o paciente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IndicadorApuradoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private IndicadorProgramatico indicador;
    private String nome;
    private String descricao;
    private TipoValorIndicador tipo;
    /** Falso quando não dá para apurar (ex.: cobertura de programa por condição, exame sem meta). */
    private boolean aplicavel;
    private String motivo;
    private long numerador;
    private Long denominador;
    /** O percentual (0 a 100, duas casas) ou a quantidade. */
    private BigDecimal valor;
    private LocalDate de;
    private LocalDate ate;
    private UUID metaId;
    private BigDecimal alvo;
    private SituacaoMetaProgramatica situacao;
}
