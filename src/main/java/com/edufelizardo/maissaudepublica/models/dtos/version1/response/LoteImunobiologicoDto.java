package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Lote de imunobiológico da sala de vacina (ADR-0148). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoteImunobiologicoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeId;
    private UUID imunobiologicoId;
    private String sigla;
    private String imunobiologicoNome;
    private String numeroLote;
    private String fabricante;
    private LocalDate validade;
    private int doses;
    private UUID camaraId;
    private String camaraNome;
    private SituacaoLoteImunobiologico situacao;
    private Instant sobSuspeitaDesde;
    private String parecer;
    private boolean vencido;
    private boolean venceEm30Dias;
    /** Disponível, na validade e com saldo. */
    private boolean aplicavel;
}
