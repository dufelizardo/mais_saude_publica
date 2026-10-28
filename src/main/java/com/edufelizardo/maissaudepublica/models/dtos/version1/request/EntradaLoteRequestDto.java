package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Entrada de imunobiológico na sala de vacina (ADR-0148): o lote é criado ou somado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EntradaLoteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID unidadeId;
    @NotNull
    private UUID imunobiologicoId;
    @NotBlank
    @Size(max = 40)
    private String numeroLote;
    @Size(max = 100)
    private String fabricante;
    @NotNull
    private LocalDate validade;
    @NotNull
    @Positive
    private Integer doses;
    private UUID camaraId;
    @Size(max = 500)
    private String justificativa;
}
