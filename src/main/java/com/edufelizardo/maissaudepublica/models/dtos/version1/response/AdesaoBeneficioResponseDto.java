package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AdesaoBeneficio;
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
public class AdesaoBeneficioResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String tipoBeneficioNome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer quantidadeDependentes;

    public static AdesaoBeneficioResponseDto fromAdesaoBeneficio(AdesaoBeneficio adesaoBeneficio) {
        return new AdesaoBeneficioResponseDto(
                adesaoBeneficio.getUuid(),
                adesaoBeneficio.getProfissional().getMatricula(),
                adesaoBeneficio.getProfissional().getNome(),
                adesaoBeneficio.getTipoBeneficio().getNome(),
                adesaoBeneficio.getDataInicio(),
                adesaoBeneficio.getDataFim(),
                adesaoBeneficio.getQuantidadeDependentes()
        );
    }
}
