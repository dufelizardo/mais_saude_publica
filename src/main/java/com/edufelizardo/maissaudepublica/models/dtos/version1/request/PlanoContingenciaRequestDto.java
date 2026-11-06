package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O plano de contingência (ADR-0157): o código só na criação; o conteúdo inteiro a cada ajuste do rascunho (as listas substituem as anteriores). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanoContingenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 40, message = "O código vai até 40 caracteres.")
    private String codigo;
    @NotBlank(message = "Informe o nome do plano.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;
    @NotNull(message = "Informe o tipo de emergência.")
    private UUID tipoId;
    @NotBlank(message = "Informe o objetivo.")
    @Size(max = 2000, message = "O objetivo vai até 2000 caracteres.")
    private String objetivo;
    @NotNull(message = "Informe o início da vigência.")
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    @NotEmpty(message = "Escolha os níveis que o plano cobre.")
    private List<UUID> nivelIds;
    private List<UUID> unidadeIds;
    @Valid
    private List<Criterio> criterios;
    @Valid
    private List<Acao> acoes;
    @Valid
    private List<Recurso> recursos;
    @Valid
    private List<Contato> contatos;

    /** Critério de ativação num nível. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Criterio implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o nível do critério.")
        private UUID nivelId;
        @NotBlank(message = "Descreva o critério.")
        @Size(max = 1000, message = "O critério vai até 1000 caracteres.")
        private String descricao;
    }

    /** Ação prevista num nível. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Acao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o nível da ação.")
        private UUID nivelId;
        @NotBlank(message = "Descreva a ação.")
        @Size(max = 1000, message = "A ação vai até 1000 caracteres.")
        private String descricao;
        @NotBlank(message = "Informe o responsável pela ação.")
        @Size(max = 200, message = "O responsável vai até 200 caracteres.")
        private String responsavel;
        @Positive(message = "O prazo é em horas, acima de zero.")
        private Integer prazoHoras;
        private UUID unidadeId;
    }

    /** Recurso necessário. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Recurso implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Descreva o recurso.")
        @Size(max = 300, message = "O recurso vai até 300 caracteres.")
        private String descricao;
        @Positive(message = "A quantidade é acima de zero.")
        private Integer quantidade;
    }

    /** Contato do plano. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Contato implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "Informe o nome do contato.")
        @Size(max = 200, message = "O nome vai até 200 caracteres.")
        private String nome;
        @Size(max = 200)
        private String papel;
        @Size(max = 30)
        private String telefone;
    }
}
