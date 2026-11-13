package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoRemanejamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** O envio de doses de um lote para a sala de vacina de outra unidade (ADR-0206). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RemanejamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o lote.")
    private UUID loteId;
    @NotNull(message = "Informe a unidade de destino.")
    private UUID unidadeDestinoId;
    @NotNull(message = "Informe as doses.")
    @Min(value = 1, message = "Remaneje ao menos uma dose.")
    private Integer doses;
    @NotNull(message = "Informe o motivo.")
    private MotivoRemanejamento motivo;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
