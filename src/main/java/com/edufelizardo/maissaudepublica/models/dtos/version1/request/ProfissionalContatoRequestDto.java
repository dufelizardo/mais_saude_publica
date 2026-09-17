package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProfissionalContatoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String telefone;
    @NotBlank(message = "E-mail do Profissional precisa ser preenchido")
    private String email;
}
