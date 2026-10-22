package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.MomentoComunicacaoMp;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/** Comunicação da internação involuntária ao Ministério Público (ADR-0123). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoMpRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o momento (admissão ou alta).")
    private MomentoComunicacaoMp momento;

    /** Sem data, agora. */
    private Instant comunicadaEm;

    /** Sem matrícula, o profissional do usuário. */
    private String responsavelMatricula;

    @Size(max = 60, message = "O protocolo vai até 60 caracteres.")
    private String protocolo;

    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
