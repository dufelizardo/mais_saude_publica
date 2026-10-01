package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaTransferencia;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransferenciaFarmaciaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private StatusTransferenciaFarmacia status;
    private String medicamentoNome;
    private String numeroLote;
    private LocalDate validade;
    private UUID loteOrigemId;
    private UUID unidadeOrigemId;
    private String unidadeOrigemNome;
    private UUID unidadeDestinoId;
    private String unidadeDestinoNome;
    private UUID loteDestinoId;

    // Envio
    private Integer quantidade;
    private String profissionalMatricula;
    private String profissionalNome;
    private String observacao;
    private Instant registradoEm;
    private String registradoPorCpf;

    // Recebimento
    private Integer quantidadeRecebida;
    private Integer quantidadeDivergente;
    private MotivoDivergenciaTransferencia motivoDivergencia;
    private String justificativaDivergencia;
    private String profissionalRecebimentoMatricula;
    private String profissionalRecebimentoNome;
    private Instant recebidoEm;
    private String recebidoPorCpf;

    // Cancelamento
    private String motivoCancelamento;
    private String profissionalCancelamentoMatricula;
    private String profissionalCancelamentoNome;
    private Instant canceladoEm;
    private String canceladoPorCpf;

    public static TransferenciaFarmaciaResponseDto fromTransferencia(TransferenciaFarmacia t) {
        return new TransferenciaFarmaciaResponseDto(
                t.getUuid(),
                t.getStatus(),
                t.getLoteOrigem().getMedicamento().getNome(),
                t.getLoteOrigem().getNumeroLote(),
                t.getLoteOrigem().getValidade(),
                t.getLoteOrigem().getUuid(),
                t.getLoteOrigem().getUnidade().getUuid(),
                t.getLoteOrigem().getUnidade().getNome(),
                t.getUnidadeDestino() != null ? t.getUnidadeDestino().getUuid() : null,
                t.getUnidadeDestino() != null ? t.getUnidadeDestino().getNome() : null,
                t.getLoteDestino() != null ? t.getLoteDestino().getUuid() : null,
                t.getQuantidade(),
                matricula(t.getProfissional()),
                nome(t.getProfissional()),
                t.getObservacao(),
                t.getRegistradoEm(),
                t.getRegistradoPorCpf(),
                t.getQuantidadeRecebida(),
                t.getQuantidadeDivergente(),
                t.getMotivoDivergencia(),
                t.getJustificativaDivergencia(),
                matricula(t.getProfissionalRecebimento()),
                nome(t.getProfissionalRecebimento()),
                t.getRecebidoEm(),
                t.getRecebidoPorCpf(),
                t.getMotivoCancelamento(),
                matricula(t.getProfissionalCancelamento()),
                nome(t.getProfissionalCancelamento()),
                t.getCanceladoEm(),
                t.getCanceladoPorCpf()
        );
    }

    private static String matricula(Profissional p) {
        return p != null ? p.getMatricula() : null;
    }

    private static String nome(Profissional p) {
        return p != null ? p.getNome() : null;
    }
}
