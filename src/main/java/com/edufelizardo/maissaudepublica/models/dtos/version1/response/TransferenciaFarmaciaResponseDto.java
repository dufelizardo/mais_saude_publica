package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
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
    private String medicamentoNome;
    private String numeroLote;
    private LocalDate validade;
    private UUID loteOrigemId;
    private UUID unidadeOrigemId;
    private String unidadeOrigemNome;
    private UUID loteDestinoId;
    private UUID unidadeDestinoId;
    private String unidadeDestinoNome;
    private Integer quantidade;
    private String profissionalMatricula;
    private String profissionalNome;
    private String observacao;
    private Instant registradoEm;
    private String registradoPorCpf;

    public static TransferenciaFarmaciaResponseDto fromTransferencia(TransferenciaFarmacia t) {
        return new TransferenciaFarmaciaResponseDto(
                t.getUuid(),
                t.getLoteOrigem().getMedicamento().getNome(),
                t.getLoteOrigem().getNumeroLote(),
                t.getLoteOrigem().getValidade(),
                t.getLoteOrigem().getUuid(),
                t.getLoteOrigem().getUnidade().getUuid(),
                t.getLoteOrigem().getUnidade().getNome(),
                t.getLoteDestino().getUuid(),
                t.getLoteDestino().getUnidade().getUuid(),
                t.getLoteDestino().getUnidade().getNome(),
                t.getQuantidade(),
                t.getProfissional().getMatricula(),
                t.getProfissional().getNome(),
                t.getObservacao(),
                t.getRegistradoEm(),
                t.getRegistradoPorCpf()
        );
    }
}
