package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AmostraExame;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoRejeicaoAmostra;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Amostra coletada (ADR-0093). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AmostraExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private MaterialExame material;
    private UUID unidadeColetaId;
    private String unidadeColetaNome;
    private UUID laboratorioId;
    private String laboratorioNome;
    private LocalDateTime coletadaEm;
    private String coletadaPorNome;
    private boolean rejeitada;
    private MotivoRejeicaoAmostra motivoRejeicao;
    private String observacaoRejeicao;
    private Instant rejeitadaEm;

    public static AmostraExameDto fromAmostra(AmostraExame a) {
        return new AmostraExameDto(a.getUuid(), a.getCodigo(), a.getMaterial(), a.getUnidadeColeta().getUuid(), a.getUnidadeColeta().getNome(),
                a.getLaboratorio().getUuid(), a.getLaboratorio().getNome(), a.getColetadaEm(), a.getColetadaPor().getNome(), a.isRejeitada(),
                a.getMotivoRejeicao(), a.getObservacaoRejeicao(), a.getRejeitadaEm());
    }
}
