package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Em que se baseou a abertura do prontuário (ADR-0076), para a tela avisar quando é um acesso justificado
 * e até quando vale. {@code base}: LIVRE (regra desligada), VINCULO, JUSTIFICADO ou PERMISSAO_AMPLA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcessoProntuarioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String base;
    /** Motivo do vínculo, em texto, ou nulo. */
    private String descricao;
    /** Só no acesso justificado: quando deixa de valer. */
    private Instant expiraEm;

    public static AcessoProntuarioDto livre() {
        return new AcessoProntuarioDto("LIVRE", null, null);
    }
}
