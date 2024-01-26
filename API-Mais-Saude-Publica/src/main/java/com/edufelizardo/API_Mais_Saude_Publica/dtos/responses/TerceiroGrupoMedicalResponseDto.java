package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors.SegundoGrupoMedicalConverterDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.TerceiroGrupoMedical;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

public record TerceiroGrupoMedicalResponseDto(
        SegundoGrupoMedicalConverterDto segundoGrupoMedical,
        String nome,
        EnderecoResponseDto enderecoResponseDto,
        Set<String> telefones,
        String email,
        Map<DayOfWeek, String> horarioFuncionamento,
        Map<DayOfWeek, String> horarioAtendimento
) {
    public static TerceiroGrupoMedicalResponseDto fromTerceiroGrupoMedicalResponse(TerceiroGrupoMedical medical) {
        EnderecoResponseDto dto = EnderecoResponseDto.fromEdereco(medical.getEndereco());
        SegundoGrupoMedicalConverterDto converterDto = SegundoGrupoMedicalConverterDto.fromSegundoGrupoMedical(medical.getSegundoGrupoMedical());
        return new TerceiroGrupoMedicalResponseDto(
                converterDto,
                medical.getNome(),
                dto,
                medical.getTelefones(),
                medical.getEmail(),
                medical.getHorarioFuncionamento(),
                medical.getHorarioAtendimento()
        );
    }

}
