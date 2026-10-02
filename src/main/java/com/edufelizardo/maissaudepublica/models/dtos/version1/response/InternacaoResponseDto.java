package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoLeito;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlta;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Internação (ADR-0098). Sem os eventos e o sumário na listagem; completa no detalhe, que é auditado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InternacaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID atendimentoId;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID leitoId;
    private String leitoIdentificacao;
    private String setorNome;
    private String medicoResponsavelMatricula;
    private String medicoResponsavelNome;
    private String cid;
    private String motivo;
    private CaraterInternacao carater;
    private Instant admitidaEm;
    private LocalDate previsaoAlta;
    private StatusInternacao status;
    private Instant altaEm;
    private TipoAlta tipoAlta;
    private String sumarioAlta;
    private String altaPorNome;
    private long diasPermanencia;
    private List<EventoLeitoDto> eventos;


    /** {@code completo} falso nas listagens: sem motivo, sumário e eventos. */
    public static InternacaoResponseDto fromInternacao(Internacao i, List<EventoLeito> eventos, boolean completo) {
        Instant fim = i.getAltaEm() != null ? i.getAltaEm() : Instant.now();
        return new InternacaoResponseDto(i.getUuid(), i.getPaciente().getUuid(), i.getPaciente().getNome(),
                i.getAtendimento() != null ? i.getAtendimento().getUuid() : null, i.getUnidade().getUuid(), i.getUnidade().getNome(),
                i.getLeito().getUuid(), i.getLeito().getIdentificacao(), i.getLeito().getSetor().getNome(),
                i.getMedicoResponsavel().getMatricula(), i.getMedicoResponsavel().getNome(), i.getCid(),
                completo ? i.getMotivo() : null, i.getCarater(), i.getAdmitidaEm(), i.getPrevisaoAlta(), i.getStatus(), i.getAltaEm(),
                i.getTipoAlta(), completo ? i.getSumarioAlta() : null, i.getAltaPor() != null ? i.getAltaPor().getNome() : null,
                Duration.between(i.getAdmitidaEm(), fim).toDays(),
                completo ? eventos.stream().map(EventoLeitoDto::fromEvento).toList() : null);
    }
}
