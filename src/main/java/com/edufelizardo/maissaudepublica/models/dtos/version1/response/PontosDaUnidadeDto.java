package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoPontoOperacional;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Os pontos operacionais da unidade (ADR-0166) e, na unidade móvel, as rotas e a localização atual. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PontosDaUnidadeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    /** Tem o perfil Atenção móvel vigente. */
    private boolean movel;
    private List<Ponto> pontos;
    private List<Ponto> pontosEncerrados;
    private List<Rota> rotas;
    private List<Rota> rotasEncerradas;
    /** A mais recente primeiro; a primeira é a localização atual. */
    private List<Posicao> posicoes;

    /** Um ponto operacional. */
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
        private TipoPontoOperacional tipo;
        private String nome;
        private String referencia;
        private Double latitude;
        private Double longitude;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }

    /** Uma rota programada, com os pontos na ordem. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Rota implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
        private List<DayOfWeek> dias;
        private LocalTime horaInicio;
        private LocalTime horaFim;
        private List<PontoRef> pontos;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }

    /** Um ponto, por nome. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class PontoRef implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
    }

    /** Uma passagem da unidade móvel. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Posicao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID pontoId;
        private String pontoNome;
        private LocalDateTime desde;
        private String observacao;
    }
}
