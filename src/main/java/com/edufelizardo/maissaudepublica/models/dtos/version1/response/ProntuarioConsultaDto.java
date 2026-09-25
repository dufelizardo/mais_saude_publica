package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Uma Consulta e seus Procedimentos, dentro da agregação do Prontuário (ver ADR-0045). Reaproveita
 * {@link ConsultaResponseDto}/{@link ProcedimentoResponseDto} — nenhum campo novo, só a composição.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProntuarioConsultaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ConsultaResponseDto consulta;
    private List<ProcedimentoResponseDto> procedimentos;
}
