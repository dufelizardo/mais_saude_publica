package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.LocalDeslocamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** O paciente deslocado na emergência (ADR-0193); na realocação, só o destino e as necessidades contam. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DeslocadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private LocalDeslocamento origemTipo;
    private UUID origemUnidadeId;
    @Size(max = 500, message = "A origem vai até 500 caracteres.")
    private String origemDescricao;
    @NotNull(message = "Informe o destino.")
    private LocalDeslocamento destinoTipo;
    private UUID destinoUnidadeId;
    private UUID destinoInstituicaoId;
    @Size(max = 500, message = "O destino vai até 500 caracteres.")
    private String destinoDescricao;
    private Instant saidaEm;
    @Size(max = 1000, message = "As necessidades vão até 1000 caracteres.")
    private String necessidades;
    @Size(max = 200, message = "O responsável vai até 200 caracteres.")
    private String responsavel;
    private UUID solicitacaoTransporteId;

    /** O retorno (com a data) ou o encerramento (com o motivo). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Fim implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Instant data;
        @Size(max = 500, message = "O motivo vai até 500 caracteres.")
        private String motivo;
    }

    /** A solicitação de transporte da T2 ligada ao deslocamento. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Transporte implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe a solicitação de transporte.")
        private UUID solicitacaoId;
    }

    /** O encaminhamento intersetorial do deslocado: o da ADR-0154 e a unidade que encaminha. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Encaminhamento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe a unidade que encaminha.")
        private UUID unidadeId;
        @NotNull(message = "Informe a instituição.")
        private UUID instituicaoId;
        @NotBlank(message = "Informe o motivo do encaminhamento.")
        @Size(max = 1000, message = "O motivo vai até 1000 caracteres.")
        private String motivo;
        private LocalDate enviadoEm;
        @NotNull(message = "Informe até quando se espera o retorno da instituição.")
        private LocalDate retornoEsperadoEm;
    }
}
