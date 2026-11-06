package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaTipoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.PerfilTipoEmergencia;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Tipo de emergência do catálogo (ADR-0156). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoEmergenciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private CategoriaTipoEmergencia categoria;
    private String cobrade;
    private PerfilTipoEmergencia perfil;
    private boolean ativo;
}
