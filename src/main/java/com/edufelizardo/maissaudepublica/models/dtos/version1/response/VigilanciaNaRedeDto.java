package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A vigilância consolidada na rede ou nas unidades do acesso (BI3, ADR-0146): agravos, semanas, regularidade e território. Só números. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VigilanciaNaRedeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private UUID unidadeId;
    private String abrangencia;
    private long notificadas;
    private long confirmadas;
    private long descartadas;
    private long emAberto;
    private long encerradas;
    private long encerradasNoPrazo;
    /** Encerradas no prazo sobre as encerradas, em %; nula sem encerrada. */
    private BigDecimal oportunidadeEncerramento;
    /** Notificações de agravo sensível que entram nos totais sem o nome (sem VIGILANCIA.INVESTIGAR). */
    private long sensiveisOcultas;
    private List<Agravo> agravos;
    private List<Semana> semanas;
    private Regularidade regularidade;
    private List<Local> territorio;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Agravo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String codigo;
        private String nome;
        private long notificadas;
        private long confirmadas;
        private long descartadas;
        private long emAberto;
        private long encerradas;
        private long encerradasNoPrazo;
        private BigDecimal oportunidadeEncerramento;
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
        private long notificacoes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Regularidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private int ano;
        private int semana;
        private int unidadesAssistenciais;
        private int comNotificacao;
        private int comNegativa;
        /** Notificaram ou declararam a negativa na semana. */
        private int regulares;
        private BigDecimal percentual;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Local implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String territorio;
        private String area;
        private String microarea;
        private long notificacoes;
    }
}
