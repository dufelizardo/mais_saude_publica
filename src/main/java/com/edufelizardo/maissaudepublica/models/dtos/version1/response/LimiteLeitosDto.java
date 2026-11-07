package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Uma capacidade de leitos da unidade (ADR-0171): quantos estão ativos, quantos são autorizados e o histórico do limite. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LimiteLeitosDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID capacidadeId;
    private String capacidadeCodigo;
    private String capacidadeNome;
    private List<TipoLeito> tiposDeLeito;
    private int ativos;
    /** Sem valor, não há limite. */
    private Integer autorizados;
    private List<Alteracao> historico;

    /** Uma alteração do limite. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Alteracao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private int quantidade;
        private String motivo;
        private LocalDateTime registradoEm;
    }
}
