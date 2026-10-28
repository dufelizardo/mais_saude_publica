package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A caderneta de vacinação do paciente (ADR-0147): as doses, o calendário com a situação e as recomendações. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CadernetaVacinacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private String pacienteNome;
    private LocalDate dataNascimento;
    private Integer idadeMeses;
    /** Sem a data de nascimento, não há como calcular. */
    private boolean situacaoDisponivel;
    private boolean emDia;
    private long atrasadas;
    private long proximas;
    private List<DoseCalendarioDto> calendario;
    /** Conforme a situação vacinal (adulto, idoso, gestante, campanhas anuais): não entram nos atrasos. */
    private List<EsquemaVacinalDto> recomendacoes;
    private List<RegistroVacinacaoDto> doses;
}
