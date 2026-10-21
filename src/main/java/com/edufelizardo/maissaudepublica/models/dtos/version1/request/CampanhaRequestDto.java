package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampanha;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Cadastro e edição da campanha (ADR-0133). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome da campanha.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o tipo da campanha.")
    private TipoCampanha tipo;

    private UUID programaId;

    @NotNull(message = "Informe o início.")
    private LocalDate inicio;

    @NotNull(message = "Informe o fim.")
    private LocalDate fim;

    @Min(value = 0, message = "A idade mínima não pode ser negativa.")
    private Integer idadeMinima;

    @Max(value = 130, message = "A idade máxima vai até 130 anos.")
    private Integer idadeMaxima;

    /** Nulo: qualquer sexo. */
    private Sexo sexo;

    private Boolean gestante;

    private List<String> condicoes;

    @Size(max = 300, message = "A descrição do público vai até 300 caracteres.")
    private String publicoDescricao;

    @Size(max = 300, message = "A meta vai até 300 caracteres.")
    private String metaDescricao;

    @Min(value = 1, message = "A meta é de pelo menos 1 pessoa.")
    private Integer metaQuantidade;
}
