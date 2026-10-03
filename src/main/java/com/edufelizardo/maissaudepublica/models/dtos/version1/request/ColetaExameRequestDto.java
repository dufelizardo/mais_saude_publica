package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** Coleta (ADR-0093): uma amostra por material. Sem itens, coleta todos os que aguardam; sem laboratório, a própria unidade analisa. */
@Data
@Getter
@Setter
public class ColetaExameRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String profissionalMatricula;

    @NotNull
    private UUID unidadeColetaId;

    private UUID laboratorioId;

    private List<UUID> itemIds;

    private LocalDateTime coletadaEm;
}
