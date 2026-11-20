package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Plano de manutenção do bem (ADR-0216). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoManutencaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private TipoManutencao tipo;
    @NotNull(message = "Informe a periodicidade.")
    @Min(value = 1, message = "A periodicidade vai de 1 a 1825 dias.")
    @Max(value = 1825, message = "A periodicidade vai de 1 a 1825 dias.")
    private Integer periodicidadeDias;
    /** A última execução antes do plano existir; sem ela, conta a partir de hoje. */
    private LocalDate ultimaExecucao;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
}
