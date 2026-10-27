package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Uma ocorrência da escala (ADR-0144): turno sem profissional, ou com o profissional afastado. Licença médica não é nomeada. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcorrenciaEscalaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID turnoId;
    private UUID unidadeId;
    private String unidadeNome;
    private String profissionalNome;
    private String funcao;
    private String tipoTurno;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    /** SEM_COBERTURA ou AFASTADO. */
    private String situacao;
    private String motivo;
}
