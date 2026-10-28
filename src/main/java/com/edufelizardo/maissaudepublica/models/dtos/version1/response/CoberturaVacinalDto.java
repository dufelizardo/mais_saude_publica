package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ClasseAbandonoVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Cobertura vacinal de uma abrangência (ADR-0149): a rede (ou as unidades do acesso), uma unidade, uma equipe ou uma
 * microárea. Sai do cadastro (as crianças adscritas), não de estimativa. Só números.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CoberturaVacinalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** REDE, UNIDADE, EQUIPE ou MICROAREA. */
    private String nivel;
    private String abrangencia;
    private UUID unidadeId;
    private UUID equipeId;
    private UUID microareaId;
    private LocalDate dataReferencia;
    /** Vacinas na meta sobre as vacinas com crianças na coorte (homogeneidade entre vacinas), em %; nulo sem coorte. */
    private Double homogeneidade;
    private int vacinasNaMeta;
    private int vacinasAvaliadas;
    private int abandonoAlto;
    private List<Vacina> vacinas;
    /** Como a abrangência se divide: Unidade, Equipe ou Microárea; nulo na microárea. */
    private String recorteRotulo;
    private List<Recorte> recortes;

    /** A cobertura de uma vacina pela dose de referência. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Vacina implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID esquemaId;
        private UUID imunobiologicoId;
        private String sigla;
        private String nome;
        private DoseVacinal dose;
        private String doseRotulo;
        private Integer idadeRecomendadaMeses;
        private BigDecimal meta;
        /** As crianças da coorte: de 1 a 12 meses depois da idade recomendada da dose. */
        private int coorte;
        private int vacinados;
        private Double cobertura;
        private boolean atingida;
        /** Recortes na meta sobre os recortes com coorte (homogeneidade entre territórios), em %. */
        private Double homogeneidadeTerritorios;
        /** Vacinas de várias doses: as da coorte com a 1ª dose, e a taxa de quem não chegou à dose de referência. */
        private Integer comPrimeiraDose;
        private Double abandono;
        private ClasseAbandonoVacinal classeAbandono;
    }

    /** Um pedaço da abrangência (a unidade na rede, a equipe na unidade, a microárea na equipe). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Recorte implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /** Nulo para "Sem microárea". */
        private UUID id;
        private String nome;
        private Double homogeneidade;
        private int vacinasNaMeta;
        private int vacinasAvaliadas;
        private List<Item> itens;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID esquemaId;
        private int coorte;
        private int vacinados;
        private Double cobertura;
        private boolean atingida;
    }
}
