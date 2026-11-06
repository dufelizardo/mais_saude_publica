package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.ViaVacina;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** Cadastro ou ajuste de imunobiológico (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ImunobiologicoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 40)
    private String codigo;
    @NotBlank
    @Size(max = 150)
    private String nome;
    @NotBlank
    @Size(max = 30)
    private String sigla;
    @NotNull
    private TipoImunobiologico tipo;
    @NotNull
    private ViaVacina via;
    @PositiveOrZero
    private BigDecimal volumeDoseMl;
    @NotNull
    @Positive
    private Integer dosesPorFrasco;
    @Positive
    private Integer validadeAposAbertoHoras;
    @Size(max = 20)
    private String codigoSipni;
    private Boolean ativo;
}
