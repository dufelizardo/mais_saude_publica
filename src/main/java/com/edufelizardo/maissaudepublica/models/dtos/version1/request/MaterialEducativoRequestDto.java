package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FormatoMaterial;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Material educativo (ADR-0174). Link só https. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MaterialEducativoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o conteúdo.")
    private UUID conteudoGrupoId;
    @NotNull(message = "Informe o formato.")
    private FormatoMaterial formato;
    @NotBlank(message = "Informe o título.")
    @Size(max = 150, message = "O título vai até 150 caracteres.")
    private String titulo;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    @Size(max = 500, message = "O link vai até 500 caracteres.")
    private String link;
}
