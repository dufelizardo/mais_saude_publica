package com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors;

import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;

public record SegundoGrupoMedicalConverterDto(
        String nome
) {
    public static SegundoGrupoMedicalConverterDto fromSegundoGrupoMedical(PrimeiroGrupoMedical medical) {
        return  new SegundoGrupoMedicalConverterDto(
                medical.getNomeInstitucional()
        );
    }
}
