package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um perfil operacional do catálogo, com quantas capacidades agrupa e em quantas unidades está vigente (ADR-0117). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerfilOperacionalResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;
    private long capacidades;
    private long unidades;
}
