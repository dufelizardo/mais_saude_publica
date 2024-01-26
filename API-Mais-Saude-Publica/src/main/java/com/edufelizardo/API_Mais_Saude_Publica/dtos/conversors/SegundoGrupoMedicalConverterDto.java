package com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors;

import com.edufelizardo.API_Mais_Saude_Publica.model.SegundoGrupoMedical;

public record SegundoGrupoMedicalConverterDto(
        String nome
) {
    public static SegundoGrupoMedicalConverterDto fromSegundoGrupoMedical(SegundoGrupoMedical medical) {
        return  new SegundoGrupoMedicalConverterDto(
                medical.getNome()
        );
    }
}
