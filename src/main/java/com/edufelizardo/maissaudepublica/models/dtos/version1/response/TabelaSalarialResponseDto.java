package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.TabelaSalarial;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTabelaSalarial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TabelaSalarialResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String cargoNome;
    private String categoriaNome;
    private BigDecimal valorBase;
    private LocalDate dataVigencia;
    private MotivoTabelaSalarial motivo;

    public static TabelaSalarialResponseDto fromTabelaSalarial(TabelaSalarial tabelaSalarial) {
        return new TabelaSalarialResponseDto(
                tabelaSalarial.getUuid(),
                tabelaSalarial.getCargo().getNome(),
                tabelaSalarial.getCargo().getCategoria().getNome(),
                tabelaSalarial.getValorBase(),
                tabelaSalarial.getDataVigencia(),
                tabelaSalarial.getMotivo()
        );
    }
}
