package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoPontoOperacional;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** O que o atendimento pode apontar como local (ADR-0166): os pontos ativos da unidade e o domicílio do paciente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LocaisDeRealizacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Ponto> pontos;
    private UUID domicilioId;
    private String domicilioDescricao;

    /** Um ponto ativo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Ponto implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
        private TipoPontoOperacional tipo;
    }
}
