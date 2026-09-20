package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoTabelaSalarial;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Getter
@Setter
public class TabelaSalarialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID cargoId;

    @NotNull
    private BigDecimal valorBase;

    @NotNull
    private LocalDate dataVigencia;

    @NotNull
    private MotivoTabelaSalarial motivo;
}
