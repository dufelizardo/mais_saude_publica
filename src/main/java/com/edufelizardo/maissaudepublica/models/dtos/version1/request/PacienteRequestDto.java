package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
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
public class PacienteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Nome do Paciente precisa ser preenchido")
    private String nome;

    @NotBlank(message = "CPF do Paciente precisa ser preenchido")
    private String cpf;

    /**
     * Cartão Nacional de Saúde (CNS) — opcional na criação: nem todo paciente (recém-nascido,
     * atendimento de urgência) tem o cartão em mãos no primeiro contato com a rede (ver DER.md,
     * seção "Modelo revisado para a próxima onda").
     */
    private String cartaoSus;

    @NotNull(message = "Data de nascimento do Paciente precisa ser preenchida")
    private LocalDate dataNascimento;

    @NotNull(message = "Sexo do Paciente precisa ser preenchido")
    private Sexo sexo;

    @NotNull(message = "Endereço do Paciente precisa ser preenchido")
    @Valid
    private EnderecoRequestDto endereco;

    @NotEmpty(message = "Ao menos um telefone de contato precisa ser preenchido")
    private Set<String> telefones;

    private String email;

    @NotNull(message = "Campo ativo precisa ser preenchido")
    private Boolean ativo;
}
