package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoExame;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um passo do pedido (ADR-0093). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private TipoEventoExame tipo;
    private UUID itemId;
    private String exameNome;
    private String texto;
    private String profissionalMatricula;
    private String profissionalNome;
    private Instant ocorridoEm;

    public static EventoExameDto fromEvento(EventoExame e) {
        return new EventoExameDto(e.getTipo(), e.getItem() != null ? e.getItem().getUuid() : null,
                e.getItem() != null ? e.getItem().getExame().getNome() : null, e.getTexto(),
                e.getProfissional().getMatricula(), e.getProfissional().getNome(), e.getOcorridoEm());
    }
}
