package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Histórico clínico consolidado de um Paciente — agregação de leitura sobre
 * Atendimento/Consulta/Procedimento (ver ADR-0039 decisão 6, ADR-0045). Não é uma entidade
 * persistida: montada sob demanda pelo {@code ProntuarioService} a partir dos dados reais.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProntuarioResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteUuid;
    private String pacienteNome;
    private List<ProntuarioAtendimentoDto> atendimentos;
}
