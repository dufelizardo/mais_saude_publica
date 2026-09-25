package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Um Atendimento e suas Consultas (cada uma com seus Procedimentos), dentro da agregação do
 * Prontuário (ver ADR-0045). Reaproveita {@link AtendimentoResponseDto} — nenhum campo novo, só a
 * composição.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProntuarioAtendimentoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private AtendimentoResponseDto atendimento;
    private List<ProntuarioConsultaDto> consultas;
}
