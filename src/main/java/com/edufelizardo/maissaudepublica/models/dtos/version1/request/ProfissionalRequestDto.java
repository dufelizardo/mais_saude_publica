package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

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
    private String telefone;
    @NotBlank(message = "E-mail do Profissional precisa ser preenchido")
    private String email;
    private LocalDate dataAdmissao;
}
