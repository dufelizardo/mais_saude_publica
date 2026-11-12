package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** A auditoria de adesão ao protocolo (ADR-0205): todos os itens respondidos. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AuditoriaProtocoloRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    private UUID setorId;
    @NotNull(message = "Informe a versão do checklist.")
    private UUID versaoId;
    private LocalDate dataAuditoria;
    @NotEmpty(message = "Responda os itens do checklist.")
    private Map<String, RespostaRoteiro> respostas;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
