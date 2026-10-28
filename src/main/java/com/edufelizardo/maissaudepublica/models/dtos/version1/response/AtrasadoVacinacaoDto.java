package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Paciente adscrito à unidade com dose atrasada (ADR-0147), para a busca ativa por microárea (ADR-0149). */
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
    /** A busca ativa por microárea (ADR-0149): a equipe e a microárea, o agente comunitário e onde mora. */
    private UUID equipeId;
    private UUID microareaId;
    private String acsNome;
    private String endereco;
}
