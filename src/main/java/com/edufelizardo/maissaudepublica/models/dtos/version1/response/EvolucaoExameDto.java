package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evolução de um exame numérico do paciente no tempo (ADR-0180): só resultados liberados, o valor atual de cada pedido
 * (retificados ficam de fora), da coleta mais antiga para a mais recente, cada ponto com a faixa que valeu nele.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EvolucaoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID exameId;
    private String exameNome;
    private String unidadeMedida;
    private List<Ponto> pontos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Ponto implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID itemId;
        private UUID pedidoId;
        /** Coleta da amostra; sem amostra, a liberação. */
        private Instant data;
        private BigDecimal valor;
        private String unidadeMedida;
        private BigDecimal referenciaMinima;
        private BigDecimal referenciaMaxima;
        private BigDecimal criticoMinimo;
        private BigDecimal criticoMaximo;
        private InterpretacaoResultado interpretacao;
        private String faixaDescricao;
        private boolean retificado;
    }
}
