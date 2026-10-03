package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Um exame laboratorial do paciente no prontuário (ADR-0095): situação de cada exame pedido e, só depois da liberação,
 * o resultado. Resultado registrado e ainda não liberado não sai daqui.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameProntuarioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID itemId;
    private UUID pedidoId;
    /** O atendimento em que o exame foi pedido; nulo no pedido avulso. */
    private UUID atendimentoId;
    private String exameNome;
    private MaterialExame material;
    private StatusItemExame status;
    private PrioridadeExame prioridade;
    private Instant solicitadoEm;
    private String unidadeSolicitanteNome;
    private String profissionalSolicitanteNome;
    private ResultadoExameDto resultado;

    public static ExameProntuarioDto fromItem(ItemPedidoExame i) {
        var p = i.getPedido();
        return new ExameProntuarioDto(i.getUuid(), p.getUuid(), p.getAtendimento() != null ? p.getAtendimento().getUuid() : null,
                i.getExame().getNome(), i.getExame().getMaterial(), i.getStatus(), p.getPrioridade(), p.getSolicitadoEm(),
                p.getUnidadeSolicitante().getNome(), p.getProfissionalSolicitante().getNome(),
                i.getStatus() == StatusItemExame.LIBERADO && i.getResultadoAtual() != null
                        ? ResultadoExameDto.fromResultado(i.getResultadoAtual()) : null);
    }
}
