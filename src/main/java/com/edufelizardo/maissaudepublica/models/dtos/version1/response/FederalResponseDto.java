package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EnderecoRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
public class FederalResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 3410068205127200223L;

    private String nome;
    private TipoUnidadeDeSaude tipo;
    private EnderecoRequestDto endereco;
    private Set<String> saudeTelefones;
    private String email;
    private Map<DayOfWeek, String> horarioFuncionamento;
    private Map<DayOfWeek, String> horarioAtendimento;

    public static FederalResponseDto fromHierarquicoResponseDto(UnidadeDeSaude unidadeDeSaude) {
        EnderecoRequestDto dto = EnderecoRequestDto.fromEndereco(unidadeDeSaude.getEndereco());
        return new FederalResponseDto(
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
