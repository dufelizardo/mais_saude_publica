package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EntregaResultadoExame;
import com.edufelizardo.maissaudepublica.models.enuns.FormaEntregaResultado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Entrega do resultado ao paciente (ADR-0181). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EntregaResultadoExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private boolean retiradoPeloPaciente;
    private String terceiroNome;
    private String terceiroDocumento;
    private String terceiroParentesco;
    private FormaEntregaResultado forma;
    private UUID unidadeId;
    private String unidadeNome;
    private Instant entregueEm;
    private String profissionalMatricula;
    private String profissionalNome;

    public static EntregaResultadoExameDto fromEntrega(EntregaResultadoExame e) {
        return new EntregaResultadoExameDto(e.getUuid(), e.isRetiradoPeloPaciente(), e.getTerceiroNome(), e.getTerceiroDocumento(),
                e.getTerceiroParentesco(), e.getForma(), e.getUnidade().getUuid(), e.getUnidade().getNome(), e.getEntregueEm(),
                e.getProfissional().getMatricula(), e.getProfissional().getNome());
    }
}
