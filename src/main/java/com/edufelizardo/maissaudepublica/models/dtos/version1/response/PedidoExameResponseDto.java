package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AmostraExame;
import com.edufelizardo.maissaudepublica.models.ComunicacaoCriticoExame;
import com.edufelizardo.maissaudepublica.models.EntregaResultadoExame;
import com.edufelizardo.maissaudepublica.models.EventoExame;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import lombok.*;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

/** O pedido com a indicação clínica, os resultados atuais, as amostras e os eventos (ADR-0093). Leitura auditada. */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PedidoExameResponseDto extends PedidoExameResumoDto {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID atendimentoId;
    private String indicacaoClinica;
    private String cid;
    private List<AmostraExameDto> amostras;
    private List<EventoExameDto> eventos;
    /** Comunicações de valor crítico e entregas ao paciente (ADR-0181). */
    private List<ComunicacaoCriticoExameDto> comunicacoesCritico;
    private List<EntregaResultadoExameDto> entregas;

    public static PedidoExameResponseDto fromPedido(PedidoExame p, List<ItemPedidoExame> itens, List<AmostraExame> amostras,
                                                    List<EventoExame> eventos, List<ComunicacaoCriticoExame> comunicacoes,
                                                    List<EntregaResultadoExame> entregas) {
        PedidoExameResponseDto dto = new PedidoExameResponseDto();
        preencher(dto, p, itens, true);
        dto.setAtendimentoId(p.getAtendimento() != null ? p.getAtendimento().getUuid() : null);
        dto.setIndicacaoClinica(p.getIndicacaoClinica());
        dto.setCid(p.getCid());
        dto.setAmostras(amostras.stream().map(AmostraExameDto::fromAmostra).toList());
        dto.setEventos(eventos.stream().map(EventoExameDto::fromEvento).toList());
        dto.setComunicacoesCritico(comunicacoes.stream().map(ComunicacaoCriticoExameDto::fromComunicacao).toList());
        dto.setEntregas(entregas.stream().map(EntregaResultadoExameDto::fromEntrega).toList());
        return dto;
    }
}
