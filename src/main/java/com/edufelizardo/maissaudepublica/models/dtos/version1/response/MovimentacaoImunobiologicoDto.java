package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoImunobiologico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Lançamento do livro de movimentação do lote (ADR-0148). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentacaoImunobiologicoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoMovimentacaoImunobiologico tipo;
    private int doses;
    private int saldoApos;
    private MotivoPerdaImunobiologico motivoPerda;
    private String justificativa;
    private UUID registroVacinacaoId;
    private UUID frascoId;
    private Instant registradoEm;
    private String registradoPorCpf;
}
