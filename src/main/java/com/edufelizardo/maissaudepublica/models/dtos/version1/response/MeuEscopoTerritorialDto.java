package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * O escopo territorial de quem está logado (ADR-0129): se está ligado, se restringe este usuário e o que ele alcança (as
 * microáreas do ACS e as áreas das equipes dos outros membros).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MeuEscopoTerritorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean ativo;
    private boolean restrito;
    private List<Item> microareas;
    private List<Item> areas;

    /** Uma microárea ou área alcançada, com a área (para a tela abrir nela). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String codigo;
        private UUID areaId;
        private String areaCodigo;
    }
}
