package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** Notificação de agravo (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;

    @NotNull(message = "Informe o agravo.")
    private UUID agravoId;

    @NotNull(message = "Informe a unidade notificante.")
    private UUID unidadeId;

    /** Sem matrícula, o profissional do usuário. */
    private String profissionalMatricula;

    private LocalDate dataPrimeirosSintomas;

    /** Sem data, hoje. */
    private LocalDate dataNotificacao;

    @NotNull(message = "Informe a origem.")
    private OrigemNotificacao origem;

    private UUID atendimentoId;

    private UUID internacaoId;

    private UUID pedidoExameId;

    /** Da crise em saúde mental (ADR-0123): a crise fica notificada. */
    private UUID criseId;

    @Size(max = 500, message = "O local provável vai até 500 caracteres.")
    private String localProvavel;

    /** As respostas da ficha específica, pela chave do campo. */
    private Map<String, Object> respostas;
}
