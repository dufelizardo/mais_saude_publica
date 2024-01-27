package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors.TerceiroGrupoMedicalConverterDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.primeiro_departamento_operacoes.PrimeiroDepartOrganizacionalTGM;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record PrimeiroDepartOrganizacionalTGMResponseDto(
        TerceiroGrupoMedicalConverterDto terceiroGrupoMedical,
        String nome,
        List<String> CompostoPelaPrefeituraRegional,
        EnderecoResponseDto enderecoResponseDto,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static PrimeiroDepartOrganizacionalTGMResponseDto fromPrimeiroDepartOrganizacionalTGMResponse(PrimeiroDepartOrganizacionalTGM primeiroDepartOrganizacionalTGM) {
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(primeiroDepartOrganizacionalTGM.getEndereco());
        TerceiroGrupoMedicalConverterDto converterDto = TerceiroGrupoMedicalConverterDto.fromTerceiroGrupoMedical(primeiroDepartOrganizacionalTGM.getTerceiroGrupoMedical());
        return  new PrimeiroDepartOrganizacionalTGMResponseDto(
                converterDto,
                primeiroDepartOrganizacionalTGM.getNome(),
                primeiroDepartOrganizacionalTGM.getCompostoPelaPrefeituraRegional(),
                dto,
                primeiroDepartOrganizacionalTGM.getTelefones(),
                primeiroDepartOrganizacionalTGM.getEmail(),
                primeiroDepartOrganizacionalTGM.getHorarioFuncionamento(),
                primeiroDepartOrganizacionalTGM.getHorarioAtendimento()
        );
    }
}
