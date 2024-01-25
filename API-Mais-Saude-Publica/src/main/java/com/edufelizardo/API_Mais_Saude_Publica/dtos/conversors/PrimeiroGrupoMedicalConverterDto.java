package com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors;

import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;

public record PrimeiroGrupoMedicalConverterDto(
        String nome
) {
    public static PrimeiroGrupoMedicalConverterDto fromPrimeiroGrupoMedical(PrimeiroGrupoMedical medical) {
        return  new PrimeiroGrupoMedicalConverterDto(
                medical.getNomeInstitucional()
        );
    }
}
