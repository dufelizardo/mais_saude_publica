package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

public record PrimeiroGrupoMedicalResponseDto(
        String nomeInstitucional,
        EnderecoResponseDto endereco,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static PrimeiroGrupoMedicalResponseDto fromPrimeiroGrupoMedical(PrimeiroGrupoMedical saude){
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(saude.getEndereco());

        return new PrimeiroGrupoMedicalResponseDto(
                saude.getNomeInstitucional(),
                dto,
                saude.getTelefones(),
                saude.getEmail(),
                saude.getHorarioFuncionamento(),
                saude.getHorarioAtendimento()
        );
    }
}
