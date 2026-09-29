package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerda;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MovimentacaoFarmaciaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID loteId;
    private String numeroLote;
    private String medicamentoNome;
    private TipoMovimentacaoFarmacia tipo;
    private Integer quantidade;
    private Integer saldoApos;
    private MotivoPerda motivoPerda;
    private String justificativa;
    private String profissionalMatricula;
    private String profissionalNome;
    private UUID dispensacaoId;
    private UUID transferenciaId;
    private Instant registradoEm;
    private String registradoPorCpf;

    public static MovimentacaoFarmaciaResponseDto fromMovimentacao(MovimentacaoFarmacia m) {
        return new MovimentacaoFarmaciaResponseDto(
                m.getUuid(),
                m.getLote().getUuid(),
                m.getLote().getNumeroLote(),
                m.getLote().getMedicamento().getNome(),
                m.getTipo(),
                m.getQuantidade(),
                m.getSaldoApos(),
                m.getMotivoPerda(),
                m.getJustificativa(),
                m.getProfissional() != null ? m.getProfissional().getMatricula() : null,
                m.getProfissional() != null ? m.getProfissional().getNome() : null,
                m.getDispensacao() != null ? m.getDispensacao().getUuid() : null,
                m.getTransferencia() != null ? m.getTransferencia().getUuid() : null,
                m.getRegistradoEm(),
                m.getRegistradoPorCpf()
        );
    }
}
