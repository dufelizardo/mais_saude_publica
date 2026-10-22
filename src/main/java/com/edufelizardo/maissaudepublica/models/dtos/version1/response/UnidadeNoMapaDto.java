package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Uma unidade com as coordenadas (ADR-0125), para os pinos do mapa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeNoMapaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private TipoUnidadeDeSaude tipo;
    private String cnes;
    private Double latitude;
    private Double longitude;
}
