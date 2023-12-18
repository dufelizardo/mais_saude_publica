package com.edufelizardo.API_Mais_Saude_Publica.dtos;

import java.time.DayOfWeek;
import java.util.Map;

public record PrimeiroGrupoMedicalDto(
        String nomeInstitucional,
        EnderecoDto endereco,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
}
