package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.RegistroPonto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroPonto;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroPontoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private TipoRegistroPonto tipo;
    private String origem;
    private LocalDateTime dataHoraProposta;
    private TipoRegistroPonto tipoProposto;
    private String justificativaCorrecao;

    public static RegistroPontoResponseDto fromRegistroPonto(RegistroPonto registroPonto) {
        return new RegistroPontoResponseDto(
                registroPonto.getUuid(),
                registroPonto.getProfissional().getMatricula(),
                registroPonto.getProfissional().getNome(),
                registroPonto.getDataHora(),
                registroPonto.getTipo(),
                registroPonto.getOrigem(),
                registroPonto.getDataHoraProposta(),
                registroPonto.getTipoProposto(),
                registroPonto.getJustificativaCorrecao()
        );
    }
}
