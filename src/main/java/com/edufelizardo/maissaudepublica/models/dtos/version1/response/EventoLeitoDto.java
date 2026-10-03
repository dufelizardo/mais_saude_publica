package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLeito;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Movimento no leito ou na internação (ADR-0098). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoLeitoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private TipoEventoLeito tipo;
    private UUID leitoId;
    private String leitoIdentificacao;
    private String texto;
    private String profissionalMatricula;
    private String profissionalNome;
    private Instant ocorridoEm;


    public static EventoLeitoDto fromEvento(EventoLeito e) {
        return new EventoLeitoDto(e.getTipo(), e.getLeito().getUuid(), e.getLeito().getIdentificacao(), e.getTexto(),
                e.getProfissional() != null ? e.getProfissional().getMatricula() : null,
                e.getProfissional() != null ? e.getProfissional().getNome() : null, e.getOcorridoEm());
    }
}
