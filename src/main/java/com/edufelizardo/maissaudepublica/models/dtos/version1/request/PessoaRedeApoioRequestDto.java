package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.PapelRedeApoio;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Pessoa ou serviço na rede de apoio do caso (ADR-0121). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PessoaRedeApoioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @NotNull(message = "Informe o papel na rede de apoio.")
    private PapelRedeApoio papel;

    @Size(max = 100, message = "O vínculo vai até 100 caracteres.")
    private String vinculo;

    @Size(max = 30, message = "O telefone vai até 30 caracteres.")
    private String telefone;

    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
