package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import jakarta.validation.constraints.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Cadastro e edição de equipe (ADR-0103). Na edição, unidade e tipo ficam. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EquipeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;

    @NotNull(message = "Informe o tipo da equipe.")
    private TipoEquipe tipo;

    @NotBlank(message = "Informe o nome da equipe.")
    @Size(max = 80, message = "O nome vai até 80 caracteres.")
    private String nome;

    @Pattern(regexp = "^\\s*(\\d{10})?\\s*$", message = "O INE tem 10 dígitos.")
    private String ine;

    private Boolean ativa;

    @Size(max = 300, message = "As microáreas vão até 300 caracteres.")
    private String microareas;

    private String coordenadorMatricula;

    private DayOfWeek reuniaoDia;

    private LocalTime reuniaoInicio;

    private LocalTime reuniaoFim;

    @Size(max = 100, message = "O local da reunião vai até 100 caracteres.")
    private String reuniaoLocal;

    /** Na eMulti, as equipes apoiadas (eSF e eAP). */
    private List<UUID> apoiadasIds;
}
