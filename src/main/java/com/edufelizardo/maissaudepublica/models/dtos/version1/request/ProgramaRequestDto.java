package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Um programa de saúde do catálogo, com o público-alvo (ADR-0118). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProgramaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Size(max = 40, message = "O código vai até 40 caracteres.")
    @Pattern(regexp = "[A-Za-z0-9_]+", message = "O código usa só letras, números e sublinhado (ex.: HIPERDIA).")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome vai até 120 caracteres.")
    private String nome;

    private String descricao;

    @NotNull(message = "Informe a categoria.")
    private UUID categoriaId;

    /** Sem valor, municipal. */
    private AbrangenciaPrograma abrangencia;

    /** Sem valor, ativo. */
    private SituacaoPrograma situacao;

    private LocalDate inicio;

    private LocalDate fim;

    /** Matrícula do profissional que coordena; vazio, sem coordenação. */
    private String coordenadorMatricula;

    /** Sem valor, sem periodicidade. */
    private PeriodicidadeAcompanhamento periodicidade;

    private Boolean sensivel;

    @Min(value = 0, message = "A idade mínima não pode ser negativa.")
    @Max(value = 130, message = "A idade mínima vai até 130 anos.")
    private Integer idadeMinima;

    @Min(value = 0, message = "A idade máxima não pode ser negativa.")
    @Max(value = 130, message = "A idade máxima vai até 130 anos.")
    private Integer idadeMaxima;

    /** Sem valor, qualquer sexo. */
    private Sexo sexo;

    private Boolean gestante;

    /** Códigos CID-10 ou CIAP-2 (ex.: I10, E11, T90). */
    private List<String> condicoes;
}
