package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors.PrimeiroGrupoMedicalConverterDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.SegundoGrupoMedical;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

public record SegundoGrupoMedicalResponseDto(
        PrimeiroGrupoMedicalConverterDto primeiroGrupoMedical,
        String nome,
        EnderecoResponseDto endereco,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static SegundoGrupoMedicalResponseDto fromSegundoGrupoMedicalResponse(SegundoGrupoMedical saude) {
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(saude.getEndereco());
        PrimeiroGrupoMedicalConverterDto converterDto = PrimeiroGrupoMedicalConverterDto.fromPrimeiroGrupoMedical(saude.getPrimeiroGrupoMedical());

        return new SegundoGrupoMedicalResponseDto(
                converterDto,
                saude.getNome(),
                dto,
                saude.getTelefones(),
                saude.getEmail(),
                saude.getHorarioFuncionamento(),
                saude.getHorarioAtendimento()
        );
    }
}
