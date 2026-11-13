package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaCausa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** A análise de causa da não conformidade, com as categorias dos 6M (ADR-0204). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnaliseCausaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Descreva a análise de causa.")
    @Size(max = 4000, message = "A análise vai até 4000 caracteres.")
    private String analiseCausa;
    private List<CategoriaCausa> categorias;
}
