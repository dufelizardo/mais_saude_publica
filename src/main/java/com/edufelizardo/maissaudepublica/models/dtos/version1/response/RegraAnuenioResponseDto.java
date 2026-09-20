package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.RegraAnuenio;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegraAnuenioResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID categoriaUuid;
    private String categoriaNome;
    private BigDecimal percentualPorAno;
    private Integer tetoAnos;

    public static RegraAnuenioResponseDto fromRegraAnuenio(RegraAnuenio regraAnuenio) {
        return new RegraAnuenioResponseDto(
                regraAnuenio.getUuid(),
                regraAnuenio.getCategoria().getUuid(),
                regraAnuenio.getCategoria().getNome(),
                regraAnuenio.getPercentualPorAno(),
                regraAnuenio.getTetoAnos()
        );
    }
}
