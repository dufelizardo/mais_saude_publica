package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaCuidado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Uma transferência do cuidado (ADR-0122). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaCuidadoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID casoId;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeOrigemId;
    private String unidadeOrigemNome;
    private UUID unidadeDestinoId;
    private String unidadeDestinoNome;
    private String resumo;
    private SituacaoTransferenciaCuidado situacao;
    private Instant propostaEm;
    private Instant decididaEm;
    private String motivoDecisao;
    private UUID casoDestinoId;
}
