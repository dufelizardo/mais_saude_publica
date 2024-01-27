package com.edufelizardo.API_Mais_Saude_Publica.dtos;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.EnderecoDto;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record PrimeiroDepartOrganizacionalTGMDto(
        String terceiroGrupoMedical,
        String nome,
        List<String> CompostoPelaPrefeituraRegional,
        EnderecoDto endereco,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
}
