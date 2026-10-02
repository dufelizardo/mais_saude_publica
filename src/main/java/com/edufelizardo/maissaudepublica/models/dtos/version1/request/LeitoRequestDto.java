package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
/** Cadastro e edição de leito (ADR-0098). Na edição, unidade e setor ficam como estão. */
public class LeitoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe o setor.")
    private UUID setorId;

    @NotBlank(message = "Informe a identificação do leito.")
    @Size(max = 60, message = "A identificação vai até 60 caracteres.")
    private String identificacao;

    @NotNull(message = "Informe o tipo do leito.")
    private TipoLeito tipo;

    @NotNull(message = "Informe quem a enfermaria recebe: MASCULINO, FEMININO ou MISTO.")
    private SexoLeito sexo;

    private Boolean ativo;
}
