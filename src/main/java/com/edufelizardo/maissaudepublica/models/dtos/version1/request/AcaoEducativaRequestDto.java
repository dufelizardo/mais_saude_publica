package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.NivelParticipacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** O detalhe educativo da ação coletiva (ADR-0195); substitui as ligações. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoEducativaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o tema.")
    @Size(max = 120, message = "O tema vai até 120 caracteres.")
    private String tema;
    private List<UUID> conteudoGrupoIds = new ArrayList<>();
    @Valid
    private List<Material> materiais = new ArrayList<>();
    private List<UUID> microareaIds = new ArrayList<>();
    private UUID campanhaComunicacaoId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Material implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o material.")
        private UUID materialId;
        @NotNull(message = "Informe a quantidade do material.")
        @Positive(message = "A quantidade do material é acima de zero.")
        private Integer quantidade;
    }

    /** A avaliação da equipe depois da ação realizada. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Avaliacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe como foi a participação.")
        private NivelParticipacao participacao;
        @Size(max = 1000, message = "A avaliação vai até 1000 caracteres.")
        private String avaliacao;
    }
}
