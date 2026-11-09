package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Realização de um exame de imagem (ADR-0182). Sem data e hora, vale o agora. */
@Data
public class RealizacaoExameImagemRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    private LocalDateTime realizadaEm;

    @Size(max = 150)
    private String equipamento;

    private Boolean contraste;

    @Size(max = 200)
    private String contrasteDescricao;

    @Size(max = 1000)
    private String observacao;
}
