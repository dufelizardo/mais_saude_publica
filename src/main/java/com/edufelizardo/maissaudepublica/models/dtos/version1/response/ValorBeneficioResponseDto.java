package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ValorBeneficio;
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
public class ValorBeneficioResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String tipoBeneficioNome;
    private BigDecimal valor;
    private LocalDate dataVigencia;
    private String motivo;

    public static ValorBeneficioResponseDto fromValorBeneficio(ValorBeneficio valorBeneficio) {
        return new ValorBeneficioResponseDto(
                valorBeneficio.getUuid(),
                valorBeneficio.getTipoBeneficio().getNome(),
                valorBeneficio.getValor(),
                valorBeneficio.getDataVigencia(),
                valorBeneficio.getMotivo()
        );
    }
}
