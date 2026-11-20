package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Conclusão da ordem de serviço (ADR-0216): o serviço, as peças e o custo; na calibração, o certificado e a validade. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConclusaoOrdemServicoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva o serviço realizado.")
    @Size(max = 1000, message = "O serviço vai até 1000 caracteres.")
    private String servicoRealizado;
    @DecimalMin(value = "0.00", message = "O custo não pode ser negativo.")
    @Digits(integer = 13, fraction = 2, message = "O custo tem até 13 dígitos e 2 casas.")
    private BigDecimal custoMaoDeObra;
    @Size(max = 60, message = "O certificado vai até 60 caracteres.")
    private String certificado;
    private LocalDate calibracaoValidaAte;
    @Valid
    private List<Peca> pecas = new ArrayList<>();

    /** Do Estoque (com o material) ou trazida pelo contrato (com a descrição). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Peca implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID materialId;
        @Size(max = 300, message = "A descrição da peça vai até 300 caracteres.")
        private String descricao;
        @NotNull(message = "Informe a quantidade da peça.")
        @Min(value = 1, message = "A quantidade da peça é ao menos 1.")
        private Integer quantidade;
        @DecimalMin(value = "0.00", message = "O custo não pode ser negativo.")
        @Digits(integer = 13, fraction = 2, message = "O custo tem até 13 dígitos e 2 casas.")
        private BigDecimal custoUnitario;
    }
}
