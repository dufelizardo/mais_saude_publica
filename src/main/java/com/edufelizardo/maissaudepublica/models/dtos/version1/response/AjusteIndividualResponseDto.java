package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AjusteIndividual;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoAjusteIndividual;
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
public class AjusteIndividualResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private BigDecimal valor;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private MotivoAjusteIndividual motivo;
    private String referencia;

    public static AjusteIndividualResponseDto fromAjusteIndividual(AjusteIndividual ajusteIndividual) {
        return new AjusteIndividualResponseDto(
                ajusteIndividual.getUuid(),
                ajusteIndividual.getProfissional().getMatricula(),
                ajusteIndividual.getProfissional().getNome(),
                ajusteIndividual.getValor(),
                ajusteIndividual.getDataInicio(),
                ajusteIndividual.getDataFim(),
                ajusteIndividual.getMotivo(),
                ajusteIndividual.getReferencia()
        );
    }
}
