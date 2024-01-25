package com.edufelizardo.API_Mais_Saude_Publica.dtos;

import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

public record SegundoGrupoMedicalDto(
        String primeiroGrupoMedical,
        String nome,
        EnderecoDto endereco,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
){
}
