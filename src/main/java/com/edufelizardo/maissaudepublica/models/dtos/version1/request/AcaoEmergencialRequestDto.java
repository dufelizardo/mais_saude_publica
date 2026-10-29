package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeAcaoEmergencial;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Ação emergencial avulsa (ADR-0158). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoEmergencialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva a ação.")
    @Size(max = 1000, message = "A ação vai até 1000 caracteres.")
    private String descricao;
    @NotBlank(message = "Informe o responsável.")
    @Size(max = 200, message = "O responsável vai até 200 caracteres.")
    private String responsavel;
    private UUID unidadeId;
    @NotNull(message = "Informe a prioridade.")
    private PrioridadeAcaoEmergencial prioridade;
    private Instant prazo;
}
