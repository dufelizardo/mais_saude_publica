package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Sugestão de notificação (ADR-0124): a rede detecta, o profissional notifica. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SugestaoNotificacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private OrigemNotificacao origem;
    private UUID referenciaId;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID agravoId;
    private String agravoCodigo;
    private String agravoNome;
    private String cid;
    private Instant detectadaEm;
}
