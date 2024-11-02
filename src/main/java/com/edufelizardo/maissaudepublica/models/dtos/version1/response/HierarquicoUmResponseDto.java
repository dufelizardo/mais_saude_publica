package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EnderecoRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class HierarquicoUmResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private int tipo;
    private String municipio;
    private String estados;
    private String administracaoSuperior;
    private EnderecoResponseDto endereco;
    private Set<String> saudeTelefones;
    private String email;
    private Map<DayOfWeek, String> horarioFuncionamento;
    private Map<DayOfWeek, String> horarioAtendimento;

    public HierarquicoUmResponseDto(String nome, int tipo, String unidadeSuperior, String municipio,
                                    String estados, EnderecoResponseDto dto, Set<String> saudeTelefones, String email,
                                    Map<DayOfWeek, String> horarioFuncionamento,
                                    Map<DayOfWeek, String> horarioAtendimento) {
        this.nome = nome;
        this.tipo = tipo;
        this.administracaoSuperior = unidadeSuperior;
        this.municipio = municipio;
        this.estados = estados;
        this.endereco = dto;
        this.saudeTelefones = saudeTelefones;
        this.email = email;
        this.horarioFuncionamento = horarioFuncionamento;
        this.horarioAtendimento = horarioAtendimento;
    }


    public static HierarquicoUmResponseDto fromHierarquicoResponseDto (UnidadeDeSaude unidadeDeSaude) {
        EnderecoResponseDto dto = EnderecoResponseDto.fromEndereco(unidadeDeSaude.getEndereco());
        String administracaoSuperior = null;
        if (unidadeDeSaude.getUnidadeSuperior() != null) {
            administracaoSuperior = unidadeDeSaude.getUnidadeSuperior().getNome();
        }
        return new HierarquicoUmResponseDto(
                unidadeDeSaude.getNome(),
                unidadeDeSaude.getTipo(),
                administracaoSuperior,
                unidadeDeSaude.getMunicipio(),
                unidadeDeSaude.getEstados(),
                dto,
                unidadeDeSaude.getSaudeTelefones(),
                unidadeDeSaude.getEmail(),
                unidadeDeSaude.getHorarioFuncionamento(),
                unidadeDeSaude.getHorarioAtendimento()
        );
    }
}
