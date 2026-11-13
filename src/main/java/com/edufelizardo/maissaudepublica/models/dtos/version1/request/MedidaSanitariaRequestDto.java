package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaSanitaria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Interdição cautelar (até 90 dias) ou apreensão de produto (ADR-0200). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaSanitariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o estabelecimento.")
    private UUID estabelecimentoId;
    @NotNull(message = "Informe o tipo da medida.")
    private TipoMedidaSanitaria tipo;
    private UUID inspecaoId;
    /** Na interdição: total ou parcial. */
    private Boolean total;
    @NotBlank(message = "Descreva a medida.")
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;
    @Size(max = 200, message = "O produto vai até 200 caracteres.")
    private String produto;
    @Size(max = 60, message = "O lote vai até 60 caracteres.")
    private String lote;
    @Size(max = 60, message = "A quantidade vai até 60 caracteres.")
    private String quantidade;
    @Size(max = 200, message = "O local de guarda vai até 200 caracteres.")
    private String localGuarda;
    private LocalDate inicio;
    private LocalDate fimPrevisto;
}
