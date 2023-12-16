package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.model.MinisterioSaude;

import java.time.DayOfWeek;
import java.util.Map;

public record MinisterioSaudeResponseDto(
        String nomeInstitucional,
        EnderecoResponseDto endereco,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static MinisterioSaudeResponseDto fromMinisterioSaude(MinisterioSaude saude){
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(saude.getEndereco());

        return new MinisterioSaudeResponseDto(
                saude.getNomeInstitucional(),
                dto,
                saude.getHorarioFuncionamento(),
                saude.getHorarioAtendimento()
        );
    }
}
