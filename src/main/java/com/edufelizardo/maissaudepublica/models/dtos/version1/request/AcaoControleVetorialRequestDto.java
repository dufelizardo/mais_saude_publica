package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoControleVetorial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ação de controle vetorial (ADR-0162): ao menos uma microárea; o caso notificado e o surto são opcionais. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoControleVetorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da ação.")
    private TipoAcaoControleVetorial tipo;
    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;
    @NotEmpty(message = "Informe ao menos uma microárea.")
    private List<UUID> microareaIds;
    @NotNull(message = "Informe a data prevista.")
    private LocalDate dataPrevista;
    @NotBlank(message = "Informe o responsável.")
    @Size(max = 200, message = "O responsável vai até 200 caracteres.")
    private String responsavel;
    private UUID notificacaoId;
    private UUID surtoId;
}
