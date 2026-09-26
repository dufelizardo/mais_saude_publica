package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Lote;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoteResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID medicamentoUuid;
    private String medicamentoNome;
    private UUID unidadeUuid;
    private String unidadeNome;
    private String numeroLote;
    private LocalDate validade;
    private Integer quantidade;

    public static LoteResponseDto fromLote(Lote lote) {
        return new LoteResponseDto(
                lote.getUuid(),
                lote.getMedicamento().getUuid(),
                lote.getMedicamento().getNome(),
                lote.getUnidade().getUuid(),
                lote.getUnidade().getNome(),
                lote.getNumeroLote(),
                lote.getValidade(),
                lote.getQuantidade()
        );
    }
}
