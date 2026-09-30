package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Uma página da trilha (ADR-0071), com o total e o resumo do filtro inteiro — não só da página —, para os
 * indicadores da tela.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PaginaAuditoriaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<EventoAuditoriaResponseDto> itens;
    private long total;
    private int pagina;
    private int tamanho;
    private Resumo resumo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Resumo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private long leituras;
        private long alteracoes;
        private long negados;
        private long logins;
        private long loginsRecusados;
    }
}
