package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Cadastro do bem que não veio de uma compra pelo sistema (ADR-0215): já tombado, com o termo de responsabilidade. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BemPatrimonialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva o bem.")
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    @NotNull(message = "Informe a classe do bem.")
    private ClasseBem classe;
    @Size(max = 80, message = "A marca vai até 80 caracteres.")
    private String marca;
    @Size(max = 80, message = "O modelo vai até 80 caracteres.")
    private String modelo;
    @Size(max = 80, message = "O número de série vai até 80 caracteres.")
    private String numeroSerie;
    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    /** Opcional; da mesma unidade do bem. */
    private UUID setorId;
    @NotNull(message = "Informe a origem.")
    private OrigemBem origem;
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O valor tem até 13 dígitos e 2 casas.")
    private BigDecimal valorAquisicao;
    @NotNull(message = "Informe a data de aquisição.")
    private LocalDate dataAquisicao;
    @Size(max = 60, message = "O documento vai até 60 caracteres.")
    private String documento;
    @NotBlank(message = "Informe a matrícula do responsável.")
    @Size(max = 30, message = "A matrícula vai até 30 caracteres.")
    private String responsavelMatricula;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
