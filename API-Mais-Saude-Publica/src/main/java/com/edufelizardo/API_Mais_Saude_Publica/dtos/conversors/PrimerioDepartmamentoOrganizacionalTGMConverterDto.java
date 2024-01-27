package com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors;

import com.edufelizardo.API_Mais_Saude_Publica.model.primeiro_departamento_operacoes.PrimeiroDepartOrganizacionalTGM;

public record PrimerioDepartmamentoOrganizacionalTGMConverterDto(
        String nome
) {
    public static PrimerioDepartmamentoOrganizacionalTGMConverterDto fromPrimerioDepartmamentoOrganizacionalTGM(PrimeiroDepartOrganizacionalTGM tgm) {
        return  new PrimerioDepartmamentoOrganizacionalTGMConverterDto(
                tgm.getNome()
        );
    }
}
