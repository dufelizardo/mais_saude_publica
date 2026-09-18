package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProfissionalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "CPF do Profissional precisa ser preenchido")
    private String cpf;
    @NotBlank(message = "Nome do Profissional precisa ser preenchido")
    private String nome;
    private String conselhoClasse;
    private String numeroConselho;
    @NotNull(message = "Endereço do Profissional precisa ser preenchido")
    @Valid
    private EnderecoRequestDto endereco;
    @NotEmpty(message = "Ao menos um telefone de contato precisa ser preenchido")
    private Set<String> telefones;
    @NotBlank(message = "E-mail do Profissional precisa ser preenchido")
    private String email;
    private LocalDate dataAdmissao;
}
