package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Um local da campanha (ADR-0133): unidade da rede ou local temporário com a unidade responsável. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LocalCampanhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** A unidade da rede, ou a responsável pelo local temporário. */
    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    private Boolean temporario;

    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @Size(max = 500, message = "O endereço vai até 500 caracteres.")
    private String endereco;

    @DecimalMin(value = "-90.0", message = "Latitude inválida.")
    @DecimalMax(value = "90.0", message = "Latitude inválida.")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude inválida.")
    @DecimalMax(value = "180.0", message = "Longitude inválida.")
    private Double longitude;

    @Size(max = 200, message = "O funcionamento vai até 200 caracteres.")
    private String funcionamento;

    private LocalDate inicio;

    private LocalDate fim;
}
