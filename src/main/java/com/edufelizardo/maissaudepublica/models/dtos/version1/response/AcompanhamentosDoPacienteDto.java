package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Os programas de um paciente (ADR-0119), lidos com as regras do prontuário: {@code acesso} diz em que a leitura se
 * baseou (vínculo, acesso justificado…). Programa sensível de unidade fora do acompanhamento de quem lê não aparece:
 * só conta em {@code restritos}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentosDoPacienteDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private AcessoProntuarioDto acesso;
    private List<AcompanhamentoDto> vigentes;
    private List<AcompanhamentoDto> encerrados;
    private int restritos;
}
