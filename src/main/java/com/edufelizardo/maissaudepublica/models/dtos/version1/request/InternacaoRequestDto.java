package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
/** Internação de um paciente num leito livre (ADR-0098). */
public class InternacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    private UUID atendimentoId;

    @NotNull(message = "Informe o leito.")
    private UUID leitoId;

    @NotBlank(message = "Informe a matrícula do médico responsável.")
    private String medicoMatricula;

    @NotBlank(message = "Informe o CID-10 principal.")
    @Pattern(regexp = "^\\s*[A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?\\s*$", message = "CID-10 inválido (ex.: J18.9).")
    private String cid;

    @NotBlank(message = "Informe o motivo da internação.")
    @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
    private String motivo;

    @NotNull(message = "Informe o caráter: ELETIVA ou URGENCIA.")
    private CaraterInternacao carater;

    private LocalDate previsaoAlta;
}
