package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FormatoMaterial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/** Campanha em andamento no portal (ADR-0174): nenhum dado de pessoa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaComunicacaoPublicaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private String objetivo;
    private LocalDate inicio;
    private LocalDate fim;
    private List<String> temas;
    private List<Conteudo> conteudos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Conteudo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String titulo;
        private String resumo;
        private List<Material> materiais;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Material implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private FormatoMaterial formato;
        private String titulo;
        private String link;
    }
}
