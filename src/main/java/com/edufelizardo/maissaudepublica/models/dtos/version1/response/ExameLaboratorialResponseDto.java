package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.FaixaReferenciaExame;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
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
    private BigDecimal criticoMinimo;
    private BigDecimal criticoMaximo;
    private String referenciaTexto;
    private String preparo;
    private Integer prazoDias;
    private boolean ativo;
    /** Faixas por sexo e idade (ADR-0180), da mais nova para a mais velha; a faixa geral vale quando nenhuma se aplica. */
    private List<FaixaReferenciaExameDto> faixas;

    public static ExameLaboratorialResponseDto fromExame(ExameLaboratorial e, List<FaixaReferenciaExame> faixas) {
        return new ExameLaboratorialResponseDto(e.getUuid(), e.getNome(), e.getMaterial(), e.getTipoResultado(), e.getUnidadeMedida(),
                e.getReferenciaMinima(), e.getReferenciaMaxima(), e.getCriticoMinimo(), e.getCriticoMaximo(), e.getReferenciaTexto(),
                e.getPreparo(), e.getPrazoDias(), e.isAtivo(), faixas.stream()
                .sorted(Comparator.comparing((FaixaReferenciaExame f) -> f.inicioAproximado())
                        .thenComparing(f -> f.getSexo() == null ? "" : f.getSexo().name()))
                .map(FaixaReferenciaExameDto::fromFaixa).toList());
    }
}
