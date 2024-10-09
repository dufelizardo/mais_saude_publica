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
public class HierarquicoZeroRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Nome da Instituição precisa ser preenchido")
    private String nome;
    @NotBlank(message = "Tipo da Instituição precisa ser preenchido")
    private String tipo;
    @Valid
    private EnderecoRequestDto endereco;
    private Set<String> telefones;
    @NotBlank(message = "E-mail da Instituição precisa ser preenchido")
    private String email;
    private Map<DayOfWeek, String> horarioFuncionamento;
    private Map<DayOfWeek, String> horarioAtendimento;

}
