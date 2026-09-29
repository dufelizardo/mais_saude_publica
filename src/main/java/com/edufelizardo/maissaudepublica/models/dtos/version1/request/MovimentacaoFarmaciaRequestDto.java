package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerda;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Lançamento manual no livro de estoque (ADR-0057) — só {@code PERDA} e {@code AJUSTE_INVENTARIO}.
 * Entrada e dispensação são lançadas pelo próprio sistema, ao criar um lote e ao dispensar.
 *
 * <ul>
 *   <li>{@code PERDA}: {@code quantidade} (positiva, sai do estoque) e {@code motivoPerda}.</li>
 *   <li>{@code AJUSTE_INVENTARIO}: {@code saldoContado} (o que a contagem física encontrou) e
 *   {@code justificativa}; a diferença para o saldo do sistema é calculada.</li>
 * </ul>
 */
@Data
@Getter
@Setter
public class MovimentacaoFarmaciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID loteId;

    @NotNull
    private TipoMovimentacaoFarmacia tipo;

    @Positive
    private Integer quantidade;

    @PositiveOrZero
    private Integer saldoContado;

    private MotivoPerda motivoPerda;

    @Size(max = 1000)
    private String justificativa;

    @NotBlank
    private String profissionalMatricula;
}
