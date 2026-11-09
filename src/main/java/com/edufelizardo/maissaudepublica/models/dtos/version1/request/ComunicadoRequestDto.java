package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeComunicado;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Comunicado em rascunho (ADR-0168). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o título.")
    @Size(max = 150, message = "O título vai até 150 caracteres.")
    private String titulo;
    @NotBlank(message = "Informe a mensagem.")
    @Size(max = 4000, message = "A mensagem vai até 4000 caracteres.")
    private String mensagem;
    @NotNull(message = "Informe a prioridade.")
    private PrioridadeComunicado prioridade;
    @NotNull(message = "Informe o público.")
    private PublicoComunicacao publico;
    @NotNull(message = "Informe o início.")
    private LocalDate inicio;
    @NotNull(message = "Informe o fim.")
    private LocalDate fim;
    private UUID unidadeId;
    private List<UUID> canalIds;
    /** Campanha de comunicação de onde sai, opcional (ADR-0174). */
    private UUID campanhaId;
}
