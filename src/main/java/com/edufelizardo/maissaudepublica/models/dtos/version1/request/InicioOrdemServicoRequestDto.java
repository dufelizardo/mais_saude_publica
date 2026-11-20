package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Início da execução da ordem de serviço (ADR-0216): interna, com o técnico, ou por contrato de serviço. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InicioOrdemServicoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe quem executa.")
    private ExecucaoManutencao execucao;
    private UUID instrumentoId;
    @Size(max = 150, message = "O técnico vai até 150 caracteres.")
    private String tecnico;
}
