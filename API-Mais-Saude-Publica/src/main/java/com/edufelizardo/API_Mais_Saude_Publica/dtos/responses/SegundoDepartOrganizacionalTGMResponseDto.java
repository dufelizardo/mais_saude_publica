package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors.PrimerioDepartmamentoOrganizacionalTGMConverterDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.segundo_departamento_operacoes.SegundoDepartOrganizacionalTGM;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

public record SegundoDepartOrganizacionalTGMResponseDto(
        PrimerioDepartmamentoOrganizacionalTGMConverterDto tgmConverterDto,
        String nome,
        EnderecoResponseDto enderecoResponseDto,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static SegundoDepartOrganizacionalTGMResponseDto fromSegundoDepartOrganizacionalTGMResponse(SegundoDepartOrganizacionalTGM tgm) {
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(tgm.getEndereco());
        PrimerioDepartmamentoOrganizacionalTGMConverterDto converterDto = PrimerioDepartmamentoOrganizacionalTGMConverterDto.fromPrimerioDepartmamentoOrganizacionalTGM(tgm.getPrimeiroDepartOrganizacionalTGM());
        return new SegundoDepartOrganizacionalTGMResponseDto(
                converterDto,
                tgm.getNome(),
                dto,
                tgm.getTelefones(),
                tgm.getEmail(),
                tgm.getHorarioFuncionamento(),
                tgm.getHorarioAtendimento()
        );
    }
}
