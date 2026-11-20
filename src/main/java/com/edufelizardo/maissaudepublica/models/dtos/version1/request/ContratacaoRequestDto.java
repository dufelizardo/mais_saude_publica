package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Abre a contratação a partir das solicitações cotadas (ADR-0213). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ContratacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o número do processo.")
    @Size(max = 60, message = "O número do processo vai até 60 caracteres.")
    private String numeroProcesso;
    @NotNull(message = "Informe a modalidade.")
    private com.edufelizardo.maissaudepublica.models.enuns.ModalidadeContratacao modalidade;
    @NotBlank(message = "Informe o objeto.")
    @Size(max = 1000, message = "O objeto vai até 1000 caracteres.")
    private String objeto;
    @Size(max = 300, message = "O fundamento legal vai até 300 caracteres.")
    private String fundamentoLegal;
    @Size(max = 200, message = "A ata de origem vai até 200 caracteres.")
    private String ataOrigem;
    @NotEmpty(message = "Informe ao menos uma solicitação cotada.")
    private List<UUID> solicitacaoIds;
}
