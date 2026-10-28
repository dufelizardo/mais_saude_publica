package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDoseCalendario;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Uma dose do calendário na caderneta do paciente, com a situação (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DoseCalendarioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID esquemaId;
    private UUID imunobiologicoId;
    private String imunobiologicoNome;
    private String sigla;
    private DoseVacinal dose;
    private String doseRotulo;
    private PublicoVacinal publico;
    private String observacao;
    /** Pela idade recomendada e pelo intervalo desde a dose anterior. */
    private LocalDate dataPrevista;
    private SituacaoDoseCalendario situacao;
    private UUID registroId;
    private LocalDateTime aplicadaEm;
}
