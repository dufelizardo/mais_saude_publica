package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoRegulacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoRegulacaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private TipoEventoRegulacao tipo;
    private StatusSolicitacaoRegulacao statusResultante;
    private PrioridadeRegulacao prioridade;
    private String texto;
    private String profissionalMatricula;
    private String profissionalNome;
    private Instant ocorridoEm;
    private String registradoPorCpf;

    public static EventoRegulacaoResponseDto fromEvento(EventoRegulacao e) {
        return new EventoRegulacaoResponseDto(e.getTipo(), e.getStatusResultante(), e.getPrioridade(), e.getTexto(),
                e.getProfissional().getMatricula(), e.getProfissional().getNome(), e.getOcorridoEm(), e.getRegistradoPorCpf());
    }
}
