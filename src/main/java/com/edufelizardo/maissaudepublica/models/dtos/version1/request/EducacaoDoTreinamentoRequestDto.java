package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** As competências e os conteúdos do treinamento (ADR-0173). Substitui as ligações atuais. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EducacaoDoTreinamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe as competências, mesmo que vazias.")
    private List<UUID> competenciaIds;
    @NotNull(message = "Informe os conteúdos, mesmo que vazios.")
    private List<UUID> conteudoGrupoIds;
}
