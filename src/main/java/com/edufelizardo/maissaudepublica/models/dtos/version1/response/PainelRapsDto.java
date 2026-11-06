package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRaps;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A Rede de Atenção Psicossocial (ADR-0153): os pontos por componente, com as modalidades, os casos e as intervenções dos últimos 90 dias, e as lacunas. Só números. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PainelRapsDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String abrangencia;
    private List<Componente> componentes;
    /** Componentes sem nenhum ponto e modalidades de CAPS que a rede não tem. */
    private List<String> lacunas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Componente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private ComponenteRaps componente;
        private String rotulo;
        private List<Ponto> unidades;
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

        private UUID unidadeId;
        private String unidadeNome;
        private String tipo;
        /** As capacidades da unidade neste componente (as modalidades de CAPS, a unidade de acolhimento). */
        private List<String> capacidades;
        private long casosEmAcompanhamento;
        /** O que a unidade oferece de fato: as intervenções realizadas nos últimos 90 dias, por tipo. */
        private List<ContagemProducaoDto> intervencoes90Dias;
    }
}
