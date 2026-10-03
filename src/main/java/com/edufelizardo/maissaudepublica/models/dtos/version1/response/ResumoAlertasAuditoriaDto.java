package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Contagens dos alertas no escopo de quem audita (ADR-0096): indicadores da tela e contador do menu. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResumoAlertasAuditoriaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long abertos;
    private long abertosAlta;
    private long ultimos7Dias;
    private long procedentes;
}
