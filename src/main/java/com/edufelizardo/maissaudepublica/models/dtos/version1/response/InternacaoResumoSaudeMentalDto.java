package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** A internação do paciente vista do caso em saúde mental, só leitura (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InternacaoResumoSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String unidadeNome;
    private Instant admitidaEm;
    private Instant altaEm;
    private StatusInternacao status;
    private ModalidadeInternacao modalidade;
}
