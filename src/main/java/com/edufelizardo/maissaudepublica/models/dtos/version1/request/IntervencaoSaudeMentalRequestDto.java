package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIntervencaoSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Intervenção realizada no caso (ADR-0121). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IntervencaoSaudeMentalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da intervenção.")
    private UUID tipoId;

    /** Sem data, agora. */
    private Instant realizadaEm;

    /** Sem situação, realizada. */
    private SituacaoIntervencaoSaudeMental situacao;

    @NotBlank(message = "Escreva o resumo da intervenção.")
    @Size(max = 2000, message = "O resumo vai até 2000 caracteres.")
    private String resumo;

    private String compartilhadoComMatricula;

    /** A intervenção prevista do PTS que esta cumpre; opcional. */
    private UUID previstaId;

    /** No matriciamento (ADR-0122): a equipe com quem o caso foi discutido; obrigatória nesse tipo. */
    private UUID equipeMatriciadaId;
}
