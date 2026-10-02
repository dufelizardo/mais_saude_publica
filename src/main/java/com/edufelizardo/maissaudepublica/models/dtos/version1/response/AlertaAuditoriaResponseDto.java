package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Um alerta da auditoria (ADR-0096), com os nomes de quem, da unidade e do paciente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AlertaAuditoriaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoAlertaAuditoria tipo;
    private SeveridadeAlertaAuditoria severidade;
    private StatusAlertaAuditoria status;
    private String sujeito;
    private String usuarioCpf;
    private String usuarioNome;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID pacienteId;
    private String pacienteNome;
    private Instant primeiroEventoEm;
    private Instant ultimoEventoEm;
    private int quantidade;
    private String descricao;
    private Instant detectadoEm;
    private String analisadoPorCpf;
    private String analisadoPorNome;
    private Instant analisadoEm;
    private String parecer;

    public static AlertaAuditoriaResponseDto fromAlerta(AlertaAuditoria a, String usuarioNome, String unidadeNome,
                                                        String pacienteNome, String analisadoPorNome) {
        return new AlertaAuditoriaResponseDto(a.getUuid(), a.getTipo(), a.getSeveridade(), a.getStatus(), a.getSujeito(),
                a.getUsuarioCpf(), usuarioNome, a.getUnidadeId(), unidadeNome, a.getPacienteId(), pacienteNome,
                a.getPrimeiroEventoEm(), a.getUltimoEventoEm(), a.getQuantidade(), a.getDescricao(), a.getDetectadoEm(),
                a.getAnalisadoPorCpf(), analisadoPorNome, a.getAnalisadoEm(), a.getParecer());
    }
}
