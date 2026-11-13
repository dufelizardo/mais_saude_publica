package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Liga um perfil ou uma capacidade do modelo operacional a um componente de rede temática (ADR-0197): um dos dois. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemComponenteRedeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID perfilId;
    private UUID capacidadeId;
}
