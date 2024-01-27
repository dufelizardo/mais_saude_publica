package com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors;

import com.edufelizardo.API_Mais_Saude_Publica.model.TerceiroGrupoMedical;

public record TerceiroGrupoMedicalConverterDto(
        String nome
) {
    public static TerceiroGrupoMedicalConverterDto fromTerceiroGrupoMedical(TerceiroGrupoMedical medical) {
        return  new TerceiroGrupoMedicalConverterDto(
                medical.getNome()
        );
    }
}
