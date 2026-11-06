package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.RecursoCapacidade;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Capacidade emergencial da unidade (ADR-0158). Sem ativa, nasce ativa e, no ajuste, mantém. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeEmergencialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    @NotNull(message = "Informe o recurso.")
    private RecursoCapacidade recurso;
    @NotNull(message = "Informe a capacidade normal.")
    @PositiveOrZero(message = "A capacidade não é negativa.")
    private Integer normal;
    @NotNull(message = "Informe quanto se pode ativar.")
    @PositiveOrZero(message = "A capacidade não é negativa.")
    private Integer emergencial;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
    private Boolean ativa;
}
