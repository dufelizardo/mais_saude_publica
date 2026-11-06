package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O acolhimento noturno da unidade (ADR-0153): as vagas, a ocupação, quem está acolhido e as saídas dos últimos 30 dias. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcolhimentoNoturnoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String unidadeNome;
    /** A unidade tem a capacidade "Acolhimento noturno". */
    private boolean temAcolhimentoNoturno;
    /** Vagas e permanência máxima; vazios sem a configuração. */
    private Integer vagas;
    private Integer permanenciaMaximaDias;
    private long ocupadas;
    private List<Permanencia> acolhidos;
    private List<Permanencia> saidasRecentes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Permanencia implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID pacienteId;
        private String pacienteNome;
        private UUID unidadeId;
        private String unidadeNome;
        private LocalDate entrada;
        private LocalDate saidaPrevista;
        private LocalDate saida;
        /** Noites desde a entrada (até a saída, ou até hoje). */
        private long dias;
        /** Em aberto e com a saída prevista vencida. */
        private boolean passouDaSaidaPrevista;
        /** A saída prevista está além da permanência máxima (com a justificativa). */
        private boolean alemDoMaximo;
        private String justificativaPrazo;
        private String motivoSaida;
    }
}
