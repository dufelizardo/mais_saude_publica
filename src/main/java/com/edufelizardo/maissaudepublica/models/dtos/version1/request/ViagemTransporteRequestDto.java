package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A viagem do transporte eletivo e as operações dela (ADR-0190). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ViagemTransporteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o veículo.")
    private UUID veiculoId;
    @NotBlank(message = "Informe a matrícula do condutor.")
    private String condutorMatricula;
    @NotNull(message = "Informe a saída prevista.")
    private Instant saidaPrevista;
    @NotNull(message = "Informe o retorno previsto.")
    private Instant retornoPrevisto;
    @Size(max = 500, message = "O destino vai até 500 caracteres.")
    private String destino;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
    @NotEmpty(message = "Inclua ao menos uma solicitação.")
    private List<UUID> solicitacoes;

    /** Os embarques de uma viagem programada. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embarques implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotEmpty(message = "Inclua ao menos uma solicitação.")
        private List<UUID> solicitacoes;
    }

    /** Saída ou chegada, com a quilometragem do painel. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quilometragem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotNull(message = "Informe a quilometragem.")
        @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
        private Integer quilometragem;
    }

    /** Quem embarcou e quem faltou. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Presencas implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotEmpty(message = "Informe ao menos um paciente.")
        @Valid
        private List<Presenca> itens;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Presenca implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotNull(message = "Informe a solicitação.")
        private UUID solicitacaoId;
        @NotNull(message = "Informe se embarcou.")
        private Boolean embarcou;
        @Size(max = 500, message = "O motivo vai até 500 caracteres.")
        private String motivo;
    }

    /** Cancelar, negar: o motivo; na recorrente, se cancela também as próximas. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Motivo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotBlank(message = "Informe o motivo.")
        @Size(max = 500, message = "O motivo vai até 500 caracteres.")
        private String motivo;
        private Boolean recorrencia;
    }
}
