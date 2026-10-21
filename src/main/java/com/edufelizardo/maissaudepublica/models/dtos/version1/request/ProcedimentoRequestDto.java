package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
public class ProcedimentoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** A consulta do procedimento; sem ela, o atendimento (ADR-0136). Uma das duas é obrigatória. */
    private UUID consultaId;

    private UUID atendimentoId;

    /** O item do catálogo geral (ADR-0136); sem ele, o tipo em texto livre. */
    private UUID procedimentoCatalogoId;

    /**
     * FK direta ao {@link com.edufelizardo.maissaudepublica.models.Profissional} pela matrícula
     * (mesma convenção de {@code ConsultaRequestDto.profissionalMatricula}, ver ADR-0034).
     */
    @NotBlank
    private String profissionalMatricula;

    /** Obrigatório sem o item do catálogo; com ele, vale o nome do catálogo. */
    private String tipo;

    private String descricao;

    @NotNull
    private LocalDateTime dataRealizacao;

    @NotNull
    private StatusProcedimento status;

    /** Complemento odontológico (ADR-0136): o dente (FDI), quando o item do catálogo pede. */
    private Integer dente;

    /** As faces, quando o item do catálogo pede. */
    private List<FaceDente> faces;
}
