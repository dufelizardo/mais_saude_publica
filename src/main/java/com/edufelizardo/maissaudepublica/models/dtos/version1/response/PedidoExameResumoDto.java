package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** O pedido sem dado clínico nem valores, para listagens (ADR-0093). */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PedidoExameResumoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeSolicitanteId;
    private String unidadeSolicitanteNome;
    private String profissionalSolicitanteMatricula;
    private String profissionalSolicitanteNome;
    private PrioridadeExame prioridade;
    private Instant solicitadoEm;
    /** AGUARDANDO_COLETA, EM_ANDAMENTO, CONCLUIDO ou CANCELADO, a partir da situação dos exames. */
    private String situacao;
    private int totalExames;
    private int liberados;
    private List<ItemPedidoExameDto> itens;

    public static PedidoExameResumoDto fromPedido(PedidoExame p, List<ItemPedidoExame> itens) {
        PedidoExameResumoDto dto = new PedidoExameResumoDto();
        preencher(dto, p, itens, false);
        return dto;
    }

    static void preencher(PedidoExameResumoDto dto, PedidoExame p, List<ItemPedidoExame> itens, boolean comResultado) {
        dto.setUuid(p.getUuid());
        dto.setPacienteId(p.getPaciente().getUuid());
        dto.setPacienteNome(p.getPaciente().getNome());
        dto.setUnidadeSolicitanteId(p.getUnidadeSolicitante().getUuid());
        dto.setUnidadeSolicitanteNome(p.getUnidadeSolicitante().getNome());
        dto.setProfissionalSolicitanteMatricula(p.getProfissionalSolicitante().getMatricula());
        dto.setProfissionalSolicitanteNome(p.getProfissionalSolicitante().getNome());
        dto.setPrioridade(p.getPrioridade());
        dto.setSolicitadoEm(p.getSolicitadoEm());
        dto.setTotalExames((int) itens.stream().filter(i -> i.getStatus() != StatusItemExame.CANCELADO).count());
        dto.setLiberados((int) itens.stream().filter(i -> i.getStatus() == StatusItemExame.LIBERADO).count());
        dto.setSituacao(situacao(itens));
        dto.setItens(itens.stream().map(i -> ItemPedidoExameDto.fromItem(i, comResultado)).toList());
    }

    static String situacao(List<ItemPedidoExame> itens) {
        List<ItemPedidoExame> ativos = itens.stream().filter(i -> i.getStatus() != StatusItemExame.CANCELADO).toList();
        if (ativos.isEmpty()) {
            return "CANCELADO";
        }
        if (ativos.stream().allMatch(i -> i.getStatus() == StatusItemExame.LIBERADO)) {
            return "CONCLUIDO";
        }
        if (ativos.stream().allMatch(i -> i.getStatus() == StatusItemExame.SOLICITADO)) {
            return "AGUARDANDO_COLETA";
        }
        return "EM_ANDAMENTO";
    }
}
