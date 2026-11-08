package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FormatoMaterial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Material educativo (ADR-0174). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MaterialEducativoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID conteudoGrupoId;
    private FormatoMaterial formato;
    private String titulo;
    private String descricao;
    private String link;
    private LocalDateTime registradoEm;
    private LocalDateTime retiradoEm;
    private String motivoRetirada;
}
