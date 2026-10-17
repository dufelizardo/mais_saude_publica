package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O programa com as participações vigentes e encerradas (ADR-0118). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProgramaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ProgramaResumoDto resumo;
    private List<Participacao> participacoes;
    private List<Participacao> participacoesEncerradas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Participacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID unidadeId;
        private String unidadeNome;
        private UUID equipeId;
        private String equipeNome;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }
}
