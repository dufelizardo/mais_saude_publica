package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TurnoLeituraTemperatura;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Leitura de temperatura da câmara (ADR-0148). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LeituraTemperaturaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Padrão: agora. */
    private LocalDateTime lidaEm;
    /** Padrão: pela hora (manhã até 12h). */
    private TurnoLeituraTemperatura turno;
    @NotNull
    private BigDecimal temperaturaAtual;
    @NotNull
    private BigDecimal temperaturaMinima;
    @NotNull
    private BigDecimal temperaturaMaxima;
    private String profissionalMatricula;
    @Size(max = 500)
    private String observacao;
}
