package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAbastecimentoAgua;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Um ponto de coleta de água (ADR-0164), com a última amostra. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PontoColetaAguaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private TipoAbastecimentoAgua tipo;
    private String local;
    private Double latitude;
    private Double longitude;
    private String microareaCodigo;
    private String areaNome;
    private boolean ativo;
    private LocalDate ultimaColeta;
    private Boolean ultimaConforme;
    private int amostras;
}
