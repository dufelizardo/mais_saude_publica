package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroEtapa;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A etapa manual cumprida, ou qualquer etapa dispensada com o motivo (ADR-0152). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroEtapaPercursoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a etapa.")
    private UUID etapaId;

    @NotNull(message = "Informe se a etapa foi cumprida ou dispensada.")
    private TipoRegistroEtapa tipo;

    private LocalDate data;

    @Size(max = 500)
    private String observacao;

    private String profissionalMatricula;
}
