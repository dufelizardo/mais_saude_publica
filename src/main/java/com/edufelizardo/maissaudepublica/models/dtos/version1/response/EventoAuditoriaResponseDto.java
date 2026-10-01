package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Evento da trilha (ADR-0070) com os nomes de quem, do paciente e da unidade, para leitura (ADR-0071). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoAuditoriaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private Instant ocorridoEm;
    private String usuarioCpf;
    private String usuarioNome;
    private AcaoAuditoria acao;
    private ResultadoAuditoria resultado;
    private String recurso;
    private String metodo;
    private String rota;
    private int statusHttp;
    private UUID registroId;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeId;
    private String unidadeNome;
    private String origemIp;
    private String detalhe;

    public static EventoAuditoriaResponseDto fromEvento(EventoAuditoria e, String usuarioNome, String pacienteNome, String unidadeNome) {
        return new EventoAuditoriaResponseDto(e.getUuid(), e.getOcorridoEm(), e.getUsuarioCpf(), usuarioNome, e.getAcao(),
                e.getResultado(), e.getRecurso(), e.getMetodo(), e.getRota(), e.getStatusHttp(), e.getRegistroId(),
                e.getPacienteId(), pacienteNome, e.getUnidadeId(), unidadeNome, e.getOrigemIp(), e.getDetalhe());
    }
}
