package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um exame do pedido, com o resultado atual quando houver (ADR-0093). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemPedidoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID exameId;
    private String exameNome;
    private MaterialExame material;
    private TipoResultadoExame tipoResultado;
    private StatusItemExame status;
    private String amostraCodigo;
    private String motivoCancelamento;
    private ResultadoExameDto resultado;

    /** {@code comResultado} falso nas listagens: o valor só sai no detalhe, que é auditado. */
    public static ItemPedidoExameDto fromItem(ItemPedidoExame i, boolean comResultado) {
        return new ItemPedidoExameDto(i.getUuid(), i.getExame().getUuid(), i.getExame().getNome(), i.getExame().getMaterial(),
                i.getExame().getTipoResultado(), i.getStatus(), i.getAmostra() != null ? i.getAmostra().getCodigo() : null,
                i.getMotivoCancelamento(),
                comResultado && i.getResultadoAtual() != null ? ResultadoExameDto.fromResultado(i.getResultadoAtual()) : null);
    }
}
