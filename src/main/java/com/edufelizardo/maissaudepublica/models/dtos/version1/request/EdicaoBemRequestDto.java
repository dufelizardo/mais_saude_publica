package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Correção dos dados descritivos do bem (ADR-0215); unidade, origem e aquisição não mudam por aqui. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EdicaoBemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva o bem.")
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    @Size(max = 80, message = "A marca vai até 80 caracteres.")
    private String marca;
    @Size(max = 80, message = "O modelo vai até 80 caracteres.")
    private String modelo;
    @Size(max = 80, message = "O número de série vai até 80 caracteres.")
    private String numeroSerie;
    /** Opcional; da mesma unidade do bem. */
    private UUID setorId;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
