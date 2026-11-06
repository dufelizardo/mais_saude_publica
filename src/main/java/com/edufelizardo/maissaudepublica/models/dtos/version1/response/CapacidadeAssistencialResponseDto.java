package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Uma capacidade assistencial do catálogo, com o perfil (grupo) e em quantas unidades está vigente (ADR-0117). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeAssistencialResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;
    /** Nulos: transversal. */
    private UUID perfilId;
    private String perfilCodigo;
    private String perfilNome;
    private long unidades;
    private com.edufelizardo.maissaudepublica.models.enuns.ComponenteRaps componenteRaps;
}
