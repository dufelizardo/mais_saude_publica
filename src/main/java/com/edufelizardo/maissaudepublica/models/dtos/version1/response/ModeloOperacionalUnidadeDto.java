package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * O modelo operacional de uma unidade (ADR-0117): perfis e capacidades, vigentes e encerrados. Os níveis de gestão
 * (federal a regional) não têm perfil.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ModeloOperacionalUnidadeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String unidadeNome;
    private TipoUnidadeDeSaude tipo;
    private boolean nivelDeGestao;
    private List<Perfil> perfis;
    private List<Capacidade> capacidades;
    private List<Perfil> perfisEncerrados;
    private List<Capacidade> capacidadesEncerradas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Perfil implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /** Id da associação (para encerrar). */
        private UUID uuid;
        private UUID perfilId;
        private String codigo;
        private String nome;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Capacidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        /** Id da associação (para encerrar). */
        private UUID uuid;
        private UUID capacidadeId;
        private String codigo;
        private String nome;
        /** Nulos: transversal. */
        private UUID perfilId;
        private String perfilNome;
        private LocalDate inicio;
        private LocalDate fim;
        private String observacao;
        private String motivoFim;
    }
}
