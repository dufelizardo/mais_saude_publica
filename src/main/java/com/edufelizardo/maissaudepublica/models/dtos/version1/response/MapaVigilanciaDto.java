package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Mapa dos casos (ADR-0130): contagem por microárea (para quem notifica), pontos dos casos (só para a investigação, e nunca
 * de agravo sensível) e a curva epidêmica por semana epidemiológica.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MapaVigilanciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private int total;
    /** Casos sem local (sem domicílio cadastrado nem ponto marcado). */
    private int semLocal;
    /** Quem lê vê os pontos (investigação). */
    private boolean pontosVisiveis;
    private List<Microarea> microareas;
    private List<Ponto> pontos;
    private List<Semana> curva;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Microarea implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID microareaId;
        private String codigo;
        private String areaCodigo;
        private String geometria;
        private int casos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Ponto implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID notificacaoId;
        private double latitude;
        private double longitude;
        private String agravoCodigo;
        private SituacaoNotificacao situacao;
        private ClassificacaoCasoEpidemiologico classificacao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Semana implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private int ano;
        private int semana;
        private LocalDate inicio;
        private int casos;
    }
}
