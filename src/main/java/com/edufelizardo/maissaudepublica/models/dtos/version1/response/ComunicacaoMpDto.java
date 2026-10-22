package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.MomentoComunicacaoMp;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Uma comunicação ao Ministério Público (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoMpDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private MomentoComunicacaoMp momento;
    private Instant comunicadaEm;
    private String responsavelNome;
    private String protocolo;
    private String observacao;
}
