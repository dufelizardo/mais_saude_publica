package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoExterna;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Instituição externa do catálogo da Intersetorialidade (ADR-0154). Sem {@code ativa}, nasce ativa e, no ajuste, mantém. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InstituicaoExternaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome da instituição.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @NotNull(message = "Informe o setor.")
    private SetorIntersetorial setor;

    @NotNull(message = "Informe o tipo.")
    private TipoInstituicaoExterna tipo;

    @Size(max = 300, message = "O endereço vai até 300 caracteres.")
    private String endereco;

    @Size(max = 30, message = "O telefone vai até 30 caracteres.")
    private String telefone;

    @Email(message = "Informe um e-mail válido.")
    @Size(max = 120, message = "O e-mail vai até 120 caracteres.")
    private String email;

    @Size(max = 120, message = "O responsável vai até 120 caracteres.")
    private String responsavel;

    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;

    private Boolean ativa;
}
