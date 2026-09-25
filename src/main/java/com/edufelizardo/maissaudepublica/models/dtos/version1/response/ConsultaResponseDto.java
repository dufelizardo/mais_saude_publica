package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoConsulta;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConsultaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID atendimentoUuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private TipoConsulta tipoConsulta;
    private String queixaPrincipal;
    private String diagnostico;
    private String receituario;
    private String examesSolicitados;
    private LocalDate retorno;

    public static ConsultaResponseDto fromConsulta(Consulta consulta) {
        return new ConsultaResponseDto(
                consulta.getUuid(),
                consulta.getAtendimento().getUuid(),
                consulta.getProfissional().getMatricula(),
                consulta.getProfissional().getNome(),
                consulta.getDataHora(),
                consulta.getTipoConsulta(),
                consulta.getQueixaPrincipal(),
                consulta.getDiagnostico(),
                consulta.getReceituario(),
                consulta.getExamesSolicitados(),
                consulta.getRetorno()
        );
    }
}
