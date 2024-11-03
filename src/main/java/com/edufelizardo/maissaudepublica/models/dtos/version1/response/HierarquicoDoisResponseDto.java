package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
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
public class HierarquicoDoisResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private int tipo;
    private String regiao;
    private String administracaoSuperior;
    private EnderecoResponseDto endereco;
    private Set<String> saudeTelefones;
    private String email;
    private Map<DayOfWeek, String> horarioFuncionamento;
    private Map<DayOfWeek, String> horarioAtendimento;

    public HierarquicoDoisResponseDto(String nome, int tipo, String unidadeSuperior, String regiao,
                                      EnderecoResponseDto dto,
                                      Set<String> saudeTelefones, String email,
                                      Map<DayOfWeek, String> horarioFuncionamento,
                                      Map<DayOfWeek, String> horarioAtendimento) {
        this.nome = nome;
        this.tipo = tipo;
        this.administracaoSuperior = unidadeSuperior;
        this.regiao = regiao;
        this.endereco = dto;
        this.saudeTelefones = saudeTelefones;
        this.email = email;
        this.horarioFuncionamento = horarioFuncionamento;
        this.horarioAtendimento = horarioAtendimento;
    }

    public static HierarquicoDoisResponseDto fromHierarquicoResponseDto (UnidadeDeSaude unidadeDeSaude) {
        EnderecoResponseDto dto = EnderecoResponseDto.fromEndereco(unidadeDeSaude.getEndereco());
        String administracaoSuperior = null;
        if (unidadeDeSaude.getUnidadeSuperior() != null) {
            administracaoSuperior = unidadeDeSaude.getUnidadeSuperior().getNome();
        }
        return new HierarquicoDoisResponseDto(
                unidadeDeSaude.getNome(),
                unidadeDeSaude.getTipo(),
                administracaoSuperior,
                unidadeDeSaude.getRegiao(),
                dto,
                unidadeDeSaude.getSaudeTelefones(),
                unidadeDeSaude.getEmail(),
                unidadeDeSaude.getHorarioFuncionamento(),
                unidadeDeSaude.getHorarioAtendimento()
        );
    }
}
