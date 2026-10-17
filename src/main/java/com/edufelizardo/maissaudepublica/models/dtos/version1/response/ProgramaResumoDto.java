package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Um programa no catálogo, com o público-alvo e quantas unidades o oferecem (ADR-0118): o cartão da tela. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProgramaResumoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private UUID categoriaId;
    private String categoriaNome;
    private AbrangenciaPrograma abrangencia;
    private SituacaoPrograma situacao;
    private LocalDate inicio;
    private LocalDate fim;
    private String coordenadorMatricula;
    private String coordenadorNome;
    private PeriodicidadeAcompanhamento periodicidade;
    private boolean sensivel;
    private Integer idadeMinima;
    private Integer idadeMaxima;
    private Sexo sexo;
    private boolean gestante;
    private List<String> condicoes;
    /** Público-alvo em texto (ex.: "60 anos ou mais"), para o cartão e a consulta pública. */
    private String publico;
    /** Unidades com participação vigente. */
    private long unidades;
}
