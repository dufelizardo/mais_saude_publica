package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoImovel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Imóvel do cadastro territorial (ADR-0127). Sem microárea, o ponto a encontra. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ImovelRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Sem valor, a microárea que contém o ponto. */
    private UUID microareaId;

    @NotNull(message = "Informe o tipo do imóvel.")
    private TipoImovel tipo;

    @Valid
    @NotNull(message = "Informe o endereço.")
    private EnderecoRequestDto endereco;

    @DecimalMin(value = "-90", message = "A latitude fica entre -90 e 90.")
    @DecimalMax(value = "90", message = "A latitude fica entre -90 e 90.")
    private Double latitude;

    @DecimalMin(value = "-180", message = "A longitude fica entre -180 e 180.")
    @DecimalMax(value = "180", message = "A longitude fica entre -180 e 180.")
    private Double longitude;

    /** Sem valor, ativo. */
    private Boolean ativo;
}
