package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.Instant;
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

    // Retificação (ADR-0062)
    private UUID retificacaoDeUuid;
    private String motivoRetificacao;
    private Instant registradoEm;
    private String registradoPorCpf;
    /** Verdadeiro quando outra versão corrige este registro — ele deixa de ser o vigente. */
    private boolean retificado;
    private UUID retificadoPorUuid;

    public static ConsultaResponseDto fromConsulta(Consulta consulta) {
        return fromConsulta(consulta, null);
    }

    /** {@code retificadoPor}: id da versão que corrige este registro, ou nulo se ele é o vigente (ADR-0062). */
    public static ConsultaResponseDto fromConsulta(Consulta consulta, UUID retificadoPor) {
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
                consulta.getRetorno(),
                consulta.getRetificacaoDe() != null ? consulta.getRetificacaoDe().getUuid() : null,
                consulta.getMotivoRetificacao(),
                consulta.getRegistradoEm(),
                consulta.getRegistradoPorCpf(),
                retificadoPor != null,
                retificadoPor
        );
    }
}
