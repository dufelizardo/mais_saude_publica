package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceCondicaoOdontologica;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Uma condição do catálogo do odontograma (ADR-0135). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CondicaoOdontologicaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private AlcanceCondicaoOdontologica alcance;
    private String cor;
    private String sigla;
    private boolean ativa;
}
