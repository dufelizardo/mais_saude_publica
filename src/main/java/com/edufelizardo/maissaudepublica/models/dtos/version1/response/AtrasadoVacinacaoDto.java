package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Paciente adscrito à unidade com dose atrasada (ADR-0147), para a busca ativa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtrasadoVacinacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private String pacienteNome;
    private LocalDate dataNascimento;
    private Integer idadeMeses;
    private String equipeNome;
    private String microareaNome;
    /** A mais antiga primeiro, como "Penta, 3ª dose". */
    private List<String> atrasadas;
    private LocalDate atrasadaDesde;
}
