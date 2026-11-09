package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TemaGestao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAreaGestao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Área da estrutura de gestão (ADR-0175). Sem ativa, nasce ativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AreaGestaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private TipoAreaGestao tipo;
    @NotNull(message = "Informe o tema.")
    private TemaGestao tema;
    @NotBlank(message = "Informe o nome.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;
    @Size(max = 20, message = "A sigla vai até 20 caracteres.")
    private String sigla;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    private UUID superiorId;
    private UUID regionalId;
    private Boolean ativa;
}
