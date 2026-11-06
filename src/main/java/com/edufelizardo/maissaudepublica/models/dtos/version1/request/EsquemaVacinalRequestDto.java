package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Cadastro ou ajuste de uma dose do calendário (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EsquemaVacinalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID imunobiologicoId;
    @NotNull
    private DoseVacinal dose;
    @NotNull
    private PublicoVacinal publico;
    @PositiveOrZero
    private Integer idadeMinimaMeses;
    @PositiveOrZero
    private Integer idadeRecomendadaMeses;
    @PositiveOrZero
    private Integer idadeMaximaMeses;
    @PositiveOrZero
    private Integer intervaloMinimoDias;
    @Positive
    private Integer repeticaoAnos;
    private Boolean contaNaSituacao;
    @Size(max = 300)
    private String observacao;
    @NotNull
    private LocalDate vigenteDesde;
    private LocalDate vigenteAte;
    private Integer ordem;
    private Boolean ativo;
    /** Vazio mantém o que está (ADR-0149). */
    private Boolean medeCobertura;
    @DecimalMin(value = "1", message = "A meta de cobertura vai de 1% a 100%.")
    @DecimalMax(value = "100", message = "A meta de cobertura vai de 1% a 100%.")
    private BigDecimal metaCobertura;
}
