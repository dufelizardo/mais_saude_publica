package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ocorrência com o fornecedor na execução da ordem (ADR-0214). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcorrenciaFornecedorRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private com.edufelizardo.maissaudepublica.models.enuns.TipoOcorrenciaFornecedor tipo;
    @NotBlank(message = "Descreva a ocorrência.")
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;
}
