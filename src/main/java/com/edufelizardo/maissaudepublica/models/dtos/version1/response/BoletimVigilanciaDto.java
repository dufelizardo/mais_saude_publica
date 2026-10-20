package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Boletim só com agregados (ADR-0124): por agravo, quantas notificações em cada situação e classificação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BoletimVigilanciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Linha> linhas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Linha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String agravoCodigo;
        private String agravoNome;
        private long notificadas;
        private long emInvestigacao;
        private long encerradas;
        private long confirmadas;
        private long descartadas;
    }
}
