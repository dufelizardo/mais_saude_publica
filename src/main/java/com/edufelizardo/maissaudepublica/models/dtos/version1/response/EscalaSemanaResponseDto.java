package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A escala da unidade numa semana, de segunda a domingo (ADR-0105): uma linha por profissional, com os turnos, as horas e
 * as ausências do RH; as vagas abertas; e as férias e licenças dos próximos 30 dias.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EscalaSemanaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String unidadeNome;
    private boolean funciona24h;
    private LocalDate inicio;
    private LocalDate fim;
    /** Horas dos turnos da unidade na semana (sem sobreaviso). */
    private double horasPrevistas;
    private int profissionais;
    private int turnos;
    private int vagasAbertas;
    private int plantoes;
    private int afastados;
    /** Profissionais com algum alerta de jornada ou descanso. */
    private int comAlerta;
    private List<Linha> linhas;
    private List<TurnoEscalaDto> vagas;
    private List<Ausencia> ausencias;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Linha implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String matricula;
        private String nome;
        private String conselho;
        private String cargo;
        private Integer jornadaSemanalHoras;
        /** Horas na semana em todas as unidades (sem sobreaviso), para comparar com a jornada. */
        private double horas;
        private double horasSobreaviso;
        private List<String> equipes;
        private List<String> alertas;
        /** Afastamentos do RH que tocam a semana. */
        private List<Ausencia> ausencias;
        /** Turnos da semana, inclusive os de outra unidade (com o nome dela). */
        private List<TurnoEscalaDto> turnos;
    }

    /** Férias, licença ou outro afastamento do RH. Sem a observação, que pode ter motivo de saúde. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Ausencia implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String matricula;
        private String nome;
        private String cargo;
        private TipoAfastamento tipo;
        private LocalDate inicio;
        private LocalDate fim;
    }
}
