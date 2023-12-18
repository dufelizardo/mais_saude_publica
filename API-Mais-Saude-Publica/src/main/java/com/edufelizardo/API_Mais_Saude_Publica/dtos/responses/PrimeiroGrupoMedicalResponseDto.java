package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;

import java.time.DayOfWeek;
import java.util.Map;

public record PrimeiroGrupoMedicalResponseDto(
        String nomeInstitucional,
        EnderecoResponseDto endereco,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static PrimeiroGrupoMedicalResponseDto fromPrimeiroGrupoMedical(PrimeiroGrupoMedical saude){
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(saude.getEndereco());

        return new PrimeiroGrupoMedicalResponseDto(
                saude.getNomeInstitucional(),
                dto,
                saude.getHorarioFuncionamento(),
                saude.getHorarioAtendimento()
        );
    }
}
