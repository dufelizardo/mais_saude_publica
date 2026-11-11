package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.TrechoTransporte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Solicitação de transporte eletivo (ADR-0190). Com agendamento, solicitação regulada ou internação, a data e o destino vêm deles; a
 * recorrência (hemodiálise, terapia seriada) usa os dias da semana e a data final.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SolicitacaoTransporteRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o paciente.")
    private UUID pacienteId;
    @NotNull(message = "Informe a unidade que pede.")
    private UUID unidadeId;
    @NotNull(message = "Informe o motivo.")
    private MotivoTransporte motivo;
    @NotNull(message = "Informe o trecho.")
    private TrechoTransporte trecho;

    private UUID agendamentoId;
    private UUID solicitacaoRegulacaoId;
    private UUID internacaoId;

    private LocalDateTime dataHora;
    @Size(max = 500, message = "O endereço de embarque vai até 500 caracteres.")
    private String enderecoEmbarque;
    private UUID destinoUnidadeId;
    @Size(max = 500, message = "O destino vai até 500 caracteres.")
    private String destino;
    @Size(max = 100, message = "O município de destino vai até 100 caracteres.")
    private String destinoMunicipio;

    private Boolean maca;
    private Boolean cadeiraDeRodas;
    private Boolean oxigenio;
    @Size(max = 150, message = "O nome do acompanhante vai até 150 caracteres.")
    private String acompanhanteNome;
    @Size(max = 30, message = "O documento do acompanhante vai até 30 caracteres.")
    private String acompanhanteDocumento;

    @Size(max = 10, message = "O CID vai até 10 caracteres.")
    private String cid;
    @Size(max = 2000, message = "A justificativa vai até 2000 caracteres.")
    private String justificativa;
    private String profissionalMatricula;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;

    /** Só na recorrente. */
    private List<DayOfWeek> diasDaSemana;
    private LocalDate recorrenteAte;
}
