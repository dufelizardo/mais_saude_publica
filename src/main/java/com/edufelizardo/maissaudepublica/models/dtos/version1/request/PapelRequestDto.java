package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Novo papel (ADR-0066): código estável em maiúsculas e as permissões do catálogo que ele reúne. */
@Data
@Getter
@Setter
public class PapelRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "Use letras, números e _ (ex.: AGENTE_COMUNITARIO)")
    @Size(max = 60)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    private String nome;

    @Size(max = 1000)
    private String descricao;

    @NotNull
    private List<String> permissoes;
}
