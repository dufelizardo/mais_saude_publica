package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoDesligamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Getter
@Setter
public class CalculoRescisaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String matriculaProfissional;

    @NotNull
    private TipoDesligamento tipoDesligamento;

    private BigDecimal avisoPrevio;
    private BigDecimal feriasVencidas;
    private BigDecimal feriasProporcionais;
    private BigDecimal decimoTerceiroProporcional;
    private BigDecimal multaFgts;

    @NotNull
    private BigDecimal total;

    private String documentoTrctUrl;
}
