package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Cadastro do fornecedor (ADR-0212): CPF ou CNPJ com o dígito verificador (400) e único (409). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FornecedorRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o CPF ou o CNPJ.")
    @Size(max = 20, message = "O CPF ou o CNPJ vai até 20 caracteres.")
    private String documento;
    @NotBlank(message = "Informe a razão social.")
    @Size(max = 200, message = "A razão social vai até 200 caracteres.")
    private String razaoSocial;
    @Size(max = 200, message = "O nome fantasia vai até 200 caracteres.")
    private String nomeFantasia;
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail vai até 150 caracteres.")
    private String email;
    @Size(max = 30, message = "O telefone vai até 30 caracteres.")
    private String telefone;
    @Size(max = 500, message = "O endereço vai até 500 caracteres.")
    private String endereco;
    /** Na edição; no cadastro, o fornecedor nasce ativo. */
    private Boolean ativo;
}
