package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Os programas de saúde consolidados na rede ou nas unidades do acesso (BI3, ADR-0146). Só números. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProgramasNaRedeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private UUID unidadeId;
    private String abrangencia;
    /** Programas sensíveis não exibidos por falta de PROGRAMA.ACOMPANHAR. */
    private int sensiveisOcultos;
    private List<Programa> programas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Programa implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID programaId;
        private String codigo;
        private String nome;
        private boolean sensivel;
        /** Unidades do escopo com participação vigente. */
        private int unidades;
        private long inscritosAtivos;
        private long novos;
        private long saidas;
        private long faltosos;
        /** Acompanhados em dia: a soma das unidades (ou a apuração da rede). */
        private long emDiaNumerador;
        private Long emDiaDenominador;
        private BigDecimal emDiaPercentual;
        private int metasAtingidas;
        private int metasEmRisco;
        private int metasAbaixo;
        private int metasSemDados;
    }
}
