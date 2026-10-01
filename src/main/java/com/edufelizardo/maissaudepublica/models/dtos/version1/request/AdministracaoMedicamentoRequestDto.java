package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Checagem de um medicamento prescrito (ADR-0064).
 *
 * <ul>
 *   <li>{@code ADMINISTRADO}: {@code loteId}, {@code dose}, {@code via} e {@code quantidade} obrigatórios;
 *   o lote precisa ser do medicamento e da unidade do atendimento.</li>
 *   <li>{@code NAO_ADMINISTRADO}: {@code motivoNaoAdministracao} obrigatório ({@code OUTRO} exige
 *   {@code observacao}); não baixa estoque.</li>
 * </ul>
 */
@Data
@Getter
@Setter
public class AdministracaoMedicamentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID atendimentoId;

    /** Consulta vigente do atendimento, com a prescrição. */
    @NotNull
    private UUID consultaId;

    @NotNull
    private UUID medicamentoId;

    @NotNull
    private SituacaoAdministracao situacao;

    private UUID loteId;

    @Size(max = 255)
    private String dose;

    private ViaAdministracao via;

    @Positive
    private Integer quantidade;

    private MotivoNaoAdministracao motivoNaoAdministracao;

    @Size(max = 1000)
    private String observacao;

    @NotNull
    private LocalDateTime dataHora;

    @NotBlank
    private String profissionalMatricula;
}
