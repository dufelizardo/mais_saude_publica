package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma ação programática (ADR-0132): individual, pelo acompanhamento; coletiva, pela unidade, com os participantes. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoProgramaticaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Do programa, ou da campanha (ADR-0133): um dos dois. */
    private UUID programaId;

    private UUID campanhaId;

    /** Ação da campanha: o local da campanha onde acontece. */
    private UUID localCampanhaId;

    @NotNull(message = "Informe o tipo da ação.")
    private TipoAcaoProgramatica tipo;

    /** Individual: o acompanhamento vigente do paciente no programa. */
    private UUID acompanhamentoId;

    /** Coletiva: a unidade que realiza (obrigatória), a equipe, o local e os participantes. */
    private UUID unidadeId;

    private UUID equipeId;

    @Size(max = 200, message = "O local vai até 200 caracteres.")
    private String local;

    private List<UUID> participantesIds;

    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;

    private String responsavelMatricula;

    @NotNull(message = "Informe a data prevista.")
    private LocalDate dataPrevista;
}
