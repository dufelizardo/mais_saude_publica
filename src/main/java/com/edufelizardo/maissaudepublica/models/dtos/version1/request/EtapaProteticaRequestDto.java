package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.EtapaProtetica;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Registro de uma etapa do tratamento protético (ADR-0139). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EtapaProteticaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a etapa.")
    private EtapaProtetica etapa;

    /** Sem data, hoje; não pode ser no futuro. */
    private LocalDate data;

    @NotBlank(message = "Informe a matrícula de quem registra.")
    private String profissionalMatricula;

    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;

    /** No envio ao laboratório, o laboratório, se ainda não foi informado. */
    private UUID laboratorioUnidadeId;

    @Size(max = 200, message = "O nome do laboratório vai até 200 caracteres.")
    private String laboratorioExterno;
}
