package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Quem a avaliação coletiva apontou com necessidade de tratamento e ainda não tem plano aberto (ADR-0140). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConvocacaoSaudeBucalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private String pacienteNome;
    private RiscoSaudeBucal risco;
    private NecessidadeTratamentoOdontologico necessidadeTratamento;
    private UUID acaoId;
    private String programaNome;
    private String local;
    private LocalDate realizadaEm;
    private Instant avaliadoEm;
}
