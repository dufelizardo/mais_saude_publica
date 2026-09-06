package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeDeSaudeEnderecoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private EnderecoRequestDto endereco;
    private Set<String> telefones;
    @NotBlank(message = "E-mail da Instituição precisa ser preenchido")
    private String email;

}
