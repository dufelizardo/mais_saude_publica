package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * Resposta de {@code GET /api/v1/auth/status} — fonte única de verdade para o frontend saber se
 * deve ou não exigir login (toggle {@code app.security.enabled}, ver ADR-0055). Sempre pública.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SecurityStatusResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean securityEnabled;
}
