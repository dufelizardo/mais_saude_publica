package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRejeicaoAmostra;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um exame na lista de trabalho do laboratório, sem valores (ADR-0093). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemTrabalhoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID itemId;
    private UUID pedidoId;
    private UUID pacienteId;
    private String pacienteNome;
    private String exameNome;
    private MaterialExame material;
    private String preparo;
    private StatusItemExame status;
    private PrioridadeExame prioridade;
    private Instant solicitadoEm;
    private String unidadeSolicitanteNome;
    private String amostraCodigo;
    private UUID laboratorioId;
    private String laboratorioNome;
    /** Na coleta: o motivo da rejeição da amostra anterior, quando o exame é recoleta (ADR-0095). Senão, nulo. */
    private MotivoRejeicaoAmostra motivoRecoleta;

    public static ItemTrabalhoExameDto fromItem(ItemPedidoExame i, MotivoRejeicaoAmostra motivoRecoleta) {
        var p = i.getPedido();
        var a = i.getAmostra();
        return new ItemTrabalhoExameDto(i.getUuid(), p.getUuid(), p.getPaciente().getUuid(), p.getPaciente().getNome(), i.getExame().getNome(),
                i.getExame().getMaterial(), i.getExame().getPreparo(), i.getStatus(), p.getPrioridade(), p.getSolicitadoEm(),
                p.getUnidadeSolicitante().getNome(), a != null ? a.getCodigo() : null,
                a != null ? a.getLaboratorio().getUuid() : null, a != null ? a.getLaboratorio().getNome() : null, motivoRecoleta);
    }
}
