package com.edufelizardo.API_Mais_Saude_Publica.dtos;

import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;

import java.time.DayOfWeek;
import java.util.Map;

public record MinisterioSaudeDto(
        String nomeInstitucional,
        EnderecoDto endereco,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
}
