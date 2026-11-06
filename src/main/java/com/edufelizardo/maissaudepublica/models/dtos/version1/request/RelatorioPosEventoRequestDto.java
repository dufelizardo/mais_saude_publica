package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** O rascunho do relatório pós-evento (ADR-0159): o texto e as recomendações (a lista substitui a anterior). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RelatorioPosEventoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 4000, message = "O resumo vai até 4000 caracteres.")
    private String resumo;
    @Size(max = 4000, message = "Os problemas vão até 4000 caracteres.")
    private String problemas;
    @Size(max = 4000, message = "As lições vão até 4000 caracteres.")
    private String licoes;
    @Valid
    private List<Recomendacao> recomendacoes;

    /** Uma recomendação; o plano, opcional, precisa ter sido usado na emergência. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Recomendacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Descreva a recomendação.")
        @Size(max = 1000, message = "A recomendação vai até 1000 caracteres.")
        private String descricao;
        @Size(max = 40)
        private String planoCodigo;
    }
}
