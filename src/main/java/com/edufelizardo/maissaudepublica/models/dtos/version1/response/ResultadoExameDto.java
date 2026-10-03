package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ResultadoExame;
import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um resultado de exame (ADR-0093). Dado de saúde. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResultadoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private BigDecimal valorNumerico;
    private String valorTexto;
    private String unidadeMedida;
    private BigDecimal referenciaMinima;
    private BigDecimal referenciaMaxima;
    private String referenciaTexto;
    private InterpretacaoResultado interpretacao;
    private String observacao;
    private String analisadoPorMatricula;
    private String analisadoPorNome;
    private Instant registradoEm;
    private String liberadoPorMatricula;
    private String liberadoPorNome;
    private Instant liberadoEm;
    private UUID retificacaoDeId;
    private String motivoRetificacao;

    public static ResultadoExameDto fromResultado(ResultadoExame r) {
        return new ResultadoExameDto(r.getUuid(), r.getValorNumerico(), r.getValorTexto(), r.getUnidadeMedida(), r.getReferenciaMinima(),
                r.getReferenciaMaxima(), r.getReferenciaTexto(), r.getInterpretacao(), r.getObservacao(),
                r.getAnalisadoPor().getMatricula(), r.getAnalisadoPor().getNome(), r.getRegistradoEm(),
                r.getLiberadoPor() != null ? r.getLiberadoPor().getMatricula() : null,
                r.getLiberadoPor() != null ? r.getLiberadoPor().getNome() : null, r.getLiberadoEm(),
                r.getRetificacaoDe() != null ? r.getRetificacaoDe().getUuid() : null, r.getMotivoRetificacao());
    }
}
