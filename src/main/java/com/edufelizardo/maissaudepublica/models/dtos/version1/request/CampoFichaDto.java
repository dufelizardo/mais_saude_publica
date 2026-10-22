package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampoFicha;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Um campo da ficha específica do agravo (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampoFichaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a chave do campo.")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{0,39}$", message = "A chave usa letras e números, até 40 caracteres.")
    private String chave;

    @NotBlank(message = "Informe o rótulo do campo.")
    @Size(max = 200, message = "O rótulo vai até 200 caracteres.")
    private String rotulo;

    @NotNull(message = "Informe o tipo do campo.")
    private TipoCampoFicha tipo;

    /** Sem valor, opcional. */
    private Boolean obrigatorio;

    /** Nas opções e na múltipla escolha. */
    private List<String> opcoes;
}
