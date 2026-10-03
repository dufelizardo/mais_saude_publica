package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoRegulacao;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import lombok.*;

import java.io.Serial;
import java.util.List;

/** O detalhe da solicitação, com o dado clínico e o histórico de eventos (ADR-0087). Leitura auditada. */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SolicitacaoRegulacaoResponseDto extends SolicitacaoRegulacaoResumoDto {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cid;
    private String justificativa;
    /** Retorno da executante (ADR-0089). Dado de saúde. */
    private String contrarreferencia;
    private List<EventoRegulacaoResponseDto> eventos;

    public static SolicitacaoRegulacaoResponseDto fromSolicitacao(SolicitacaoRegulacao s, Integer posicaoNaFila,
                                                                 List<EventoRegulacao> eventos) {
        SolicitacaoRegulacaoResponseDto dto = new SolicitacaoRegulacaoResponseDto();
        preencher(dto, s, posicaoNaFila);
        dto.setCid(s.getCid());
        dto.setJustificativa(s.getJustificativa());
        dto.setContrarreferencia(s.getContrarreferencia());
        dto.setEventos(eventos.stream().map(EventoRegulacaoResponseDto::fromEvento).toList());
        return dto;
    }
}
