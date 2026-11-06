package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/** Conteúdo educativo em rascunho (ADR-0168). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConteudoEducativoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o título.")
    @Size(max = 150, message = "O título vai até 150 caracteres.")
    private String titulo;
    @NotBlank(message = "Informe o resumo.")
    @Size(max = 500, message = "O resumo vai até 500 caracteres.")
    private String resumo;
    @NotBlank(message = "Informe o texto.")
    @Size(max = 20000, message = "O texto vai até 20000 caracteres.")
    private String texto;
    @NotBlank(message = "Informe o tema.")
    @Size(max = 100, message = "O tema vai até 100 caracteres.")
    private String tema;
    @NotNull(message = "Informe o público.")
    private PublicoComunicacao publico;
    @Size(max = 60, message = "A faixa etária vai até 60 caracteres.")
    private String faixaEtaria;
    @NotNull(message = "Informe a validade.")
    private LocalDate validade;
}
