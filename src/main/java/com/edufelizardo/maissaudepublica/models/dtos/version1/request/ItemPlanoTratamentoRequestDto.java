package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Um item do plano de tratamento (ADR-0137): o procedimento previsto, o dente e as faces. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemPlanoTratamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o procedimento previsto do catálogo.")
    private UUID procedimentoCatalogoId;

    private Integer dente;

    private List<FaceDente> faces;

    /** A sessão sugerida; sem ela, depois dos itens que já existem. */
    private Integer ordem;

    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
