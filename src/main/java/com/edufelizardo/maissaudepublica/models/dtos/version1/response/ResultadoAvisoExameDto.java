package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Um resultado liberado aguardando ciência do solicitante ou comunicação do crítico (ADR-0181). Sem o valor: ele sai no
 * detalhe do pedido, que é auditado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResultadoAvisoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID itemId;
    private UUID pedidoId;
    private UUID pacienteId;
    private String pacienteNome;
    private String exameNome;
    private InterpretacaoResultado interpretacao;
    private boolean critico;
    private boolean retificado;
    private Instant liberadoEm;
    private PrioridadeExame prioridade;
    private String solicitanteMatricula;
    private String solicitanteNome;
    private String unidadeSolicitanteNome;
    /** Quando o laboratório comunicou o crítico; nulo se ainda não comunicou ou se não é crítico. */
    private Instant comunicadoEm;

    public static ResultadoAvisoExameDto fromItem(ItemPedidoExame i, Instant comunicadoEm) {
        var r = i.getResultadoAtual();
        var p = i.getPedido();
        return new ResultadoAvisoExameDto(i.getUuid(), p.getUuid(), p.getPaciente().getUuid(), p.getPaciente().getNome(),
                i.getExame().getNome(), r.getInterpretacao(), critico(r.getInterpretacao()), r.getRetificacaoDe() != null,
                r.getLiberadoEm(), p.getPrioridade(), p.getProfissionalSolicitante().getMatricula(),
                p.getProfissionalSolicitante().getNome(), p.getUnidadeSolicitante().getNome(), comunicadoEm);
    }

    public static boolean critico(InterpretacaoResultado i) {
        return i == InterpretacaoResultado.CRITICO_ALTO || i == InterpretacaoResultado.CRITICO_BAIXO;
    }
}
