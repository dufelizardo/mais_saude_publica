package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ComunicacaoCriticoExame;
import com.edufelizardo.maissaudepublica.models.enuns.MeioComunicacaoCritico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Comunicação de valor crítico registrada pelo laboratório (ADR-0181). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoCriticoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID itemId;
    private UUID resultadoId;
    private String exameNome;
    private String receptorNome;
    private MeioComunicacaoCritico meio;
    private Instant comunicadoEm;
    private String profissionalMatricula;
    private String profissionalNome;

    public static ComunicacaoCriticoExameDto fromComunicacao(ComunicacaoCriticoExame c) {
        return new ComunicacaoCriticoExameDto(c.getUuid(), c.getItem().getUuid(), c.getResultado().getUuid(), c.getItem().getExame().getNome(),
                c.getReceptorNome(), c.getMeio(), c.getComunicadoEm(), c.getProfissional().getMatricula(), c.getProfissional().getNome());
    }
}
