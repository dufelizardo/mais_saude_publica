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
    /** Em que se baseou a abertura (ADR-0076): vínculo, acesso justificado, permissão ampla ou regra desligada. */
    private AcessoProntuarioDto acesso;
    /** Exames laboratoriais do paciente, do pedido mais recente ao mais antigo; resultado só depois de liberado (ADR-0095). */
    private List<ExameProntuarioDto> exames;
}
