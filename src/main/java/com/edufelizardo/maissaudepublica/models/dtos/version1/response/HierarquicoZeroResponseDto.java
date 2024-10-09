package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EnderecoRequestDto;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class HierarquicoZeroResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 3410068205127200223L;

    public String nome;
    public String tipo;
    private EnderecoRequestDto endereco;
    private Set<String> saudeTelefones;
    private String email;
    private Map<DayOfWeek, String> horarioFuncionamento;
    private Map<DayOfWeek, String> horarioAtendimento;

    public static HierarquicoZeroResponseDto fromHierarquicoResponseDto(UnidadeDeSaude unidadeDeSaude) {
        EnderecoRequestDto dto = EnderecoRequestDto.fromEndereco(unidadeDeSaude.getEndereco());
        return new HierarquicoZeroResponseDto(
                unidadeDeSaude.getNome(),
                unidadeDeSaude.getTipo(),
                dto,
                unidadeDeSaude.getSaudeTelefones(),
                unidadeDeSaude.getEmail(),
                unidadeDeSaude.getHorarioFuncionamento(),
                unidadeDeSaude.getHorarioAtendimento()
        );
    }
}
