package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A equipe com os membros, a coordenação, a reunião, as apoiadas e o histórico de membros (ADR-0103). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EquipeResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private EquipeResumoDto resumo;
    private String coordenadorMatricula;
    private DayOfWeek reuniaoDia;
    private LocalTime reuniaoInicio;
    private LocalTime reuniaoFim;
    private String reuniaoLocal;
    private List<Membro> membros;
    private List<Membro> antigos;
    private List<Vinculo> apoiadas;
    /** As eMulti que apoiam esta equipe. */
    private List<Vinculo> apoiadaPor;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Membro implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String matricula;
        private String nome;
        private String conselho;
        private FuncaoEquipe funcao;
        private String microarea;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoSaida;
        private String cargo;
        private Integer jornadaSemanalHoras;
        /** Afastamento vigente do RH (férias, licença…), ou nulo. */
        private String afastamento;
        private LocalDate afastadoAte;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Vinculo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String nome;
        private String tipo;
        private String unidadeNome;
    }
}
