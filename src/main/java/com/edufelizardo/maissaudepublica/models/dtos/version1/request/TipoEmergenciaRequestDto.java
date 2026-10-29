package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaTipoEmergencia;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Tipo de emergência do catálogo (ADR-0156). Sem {@code ativo}, nasce ativo e, no ajuste, mantém. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoEmergenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Size(max = 40, message = "O código vai até 40 caracteres.")
    private String codigo;
    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;
    @NotNull(message = "Informe a categoria.")
    private CategoriaTipoEmergencia categoria;
    @Size(max = 20, message = "O código COBRADE vai até 20 caracteres.")
    private String cobrade;
    private Boolean ativo;
}
