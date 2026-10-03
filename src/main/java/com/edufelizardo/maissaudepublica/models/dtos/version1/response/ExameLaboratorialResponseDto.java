package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Exame do catálogo (ADR-0093). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameLaboratorialResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private MaterialExame material;
    private TipoResultadoExame tipoResultado;
    private String unidadeMedida;
    private BigDecimal referenciaMinima;
    private BigDecimal referenciaMaxima;
    private String referenciaTexto;
    private String preparo;
    private Integer prazoDias;
    private boolean ativo;

    public static ExameLaboratorialResponseDto fromExame(ExameLaboratorial e) {
        return new ExameLaboratorialResponseDto(e.getUuid(), e.getNome(), e.getMaterial(), e.getTipoResultado(), e.getUnidadeMedida(),
                e.getReferenciaMinima(), e.getReferenciaMaxima(), e.getReferenciaTexto(), e.getPreparo(), e.getPrazoDias(), e.isAtivo());
    }
}
