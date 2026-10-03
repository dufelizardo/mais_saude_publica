package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Cadastro e edição de unidade pelas rotas por id (ADR-0101). Na edição, tipo e unidade superior ficam. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeCadastroRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome da unidade.")
    @Size(max = 255, message = "O nome vai até 255 caracteres.")
    private String nome;

    @NotNull(message = "Informe o tipo da unidade.")
    private TipoUnidadeDeSaude tipo;

    @Pattern(regexp = "^\\s*(\\d{7})?\\s*$", message = "O CNES tem 7 dígitos.")
    private String cnes;

    private UUID unidadeSuperiorId;

    private UUID supervisaoRegionalId;

    @Valid
    private EnderecoRequestDto endereco;

    private Set<String> telefones;

    @Email(message = "E-mail inválido.")
    private String email;

    private String responsavelCpf;
}
