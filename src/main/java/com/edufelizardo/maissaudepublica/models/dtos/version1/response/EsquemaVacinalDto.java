package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Uma dose do calendário vacinal (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EsquemaVacinalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID imunobiologicoId;
    private String imunobiologicoNome;
    private String sigla;
    private DoseVacinal dose;
    private String doseRotulo;
    private PublicoVacinal publico;
    private Integer idadeMinimaMeses;
    private Integer idadeRecomendadaMeses;
    private Integer idadeMaximaMeses;
    private Integer intervaloMinimoDias;
    private Integer repeticaoAnos;
    private boolean contaNaSituacao;
    private String observacao;
    private LocalDate vigenteDesde;
    private LocalDate vigenteAte;
    private int ordem;
    private boolean ativo;
}
