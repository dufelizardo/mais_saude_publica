package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.TipoBeneficio;
import com.edufelizardo.maissaudepublica.models.enuns.CusteioBeneficio;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoBeneficioResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private CusteioBeneficio custeio;

    public static TipoBeneficioResponseDto fromTipoBeneficio(TipoBeneficio tipoBeneficio) {
        return new TipoBeneficioResponseDto(
                tipoBeneficio.getUuid(),
                tipoBeneficio.getNome(),
                tipoBeneficio.getCusteio()
        );
    }
}
