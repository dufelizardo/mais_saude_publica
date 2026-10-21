package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Um registro do odontograma (ADR-0135), com a origem e a anulação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroOdontogramaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private int dente;
    private FaceDente face;
    private UUID condicaoId;
    private String condicaoCodigo;
    private String condicaoNome;
    private String cor;
    private String sigla;
    private LocalDate data;
    private UUID avaliacaoId;
    private UUID atendimentoId;
    private String profissionalNome;
    private String observacao;
    private boolean anulado;
    private Instant anuladoEm;
    private String motivoAnulacao;
    private Instant registradoEm;
    /** Origem procedimento (ADR-0136): qual procedimento mudou a condição. */
    private UUID procedimentoId;
    private String procedimentoNome;
}
