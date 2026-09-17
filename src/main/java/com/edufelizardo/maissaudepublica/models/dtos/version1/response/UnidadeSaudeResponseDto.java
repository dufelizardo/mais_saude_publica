package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
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
@ToString
@EqualsAndHashCode
public class UnidadeSaudeResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nome;
    private TipoUnidadeDeSaude tipo;
    private String administracaoSuperior;
    private String supervisaoRegional;
    private String responsavelCpf;
    private String responsavelNome;
    private EnderecoResponseDto endereco;
    private Set<String> saudeTelefones;
    private String email;
    private Map<DayOfWeek, String> horarioFuncionamento;
    private Map<DayOfWeek, String> horarioAtendimento;

    public UnidadeSaudeResponseDto(String nome, TipoUnidadeDeSaude tipo, String administracaoSuperior,
                                    String supervisaoRegional, String responsavelCpf, String responsavelNome,
                                    EnderecoResponseDto endereco, Set<String> saudeTelefones, String email,
                                    Map<DayOfWeek, String> horarioFuncionamento,
                                    Map<DayOfWeek, String> horarioAtendimento) {
        this.nome = nome;
        this.tipo = tipo;
        this.administracaoSuperior = administracaoSuperior;
        this.supervisaoRegional = supervisaoRegional;
        this.responsavelCpf = responsavelCpf;
        this.responsavelNome = responsavelNome;
        this.endereco = endereco;
        this.saudeTelefones = saudeTelefones;
        this.email = email;
        this.horarioFuncionamento = horarioFuncionamento;
        this.horarioAtendimento = horarioAtendimento;
    }

    public static UnidadeSaudeResponseDto fromHierarquicoResponseDto(UnidadeDeSaude unidadeDeSaude) {
        EnderecoResponseDto endereco = EnderecoResponseDto.fromEndereco(unidadeDeSaude.getEndereco());
        String administracaoSuperior = null;
        if (unidadeDeSaude.getUnidadeSuperior() != null) {
            administracaoSuperior = unidadeDeSaude.getUnidadeSuperior().getNome();
        }
        String supervisaoRegional = null;
        if (unidadeDeSaude.getSupervisaoRegional() != null) {
            supervisaoRegional = unidadeDeSaude.getSupervisaoRegional().getNome();
        }
        String responsavelNome = null;
        if (unidadeDeSaude.getResponsavel() != null) {
            responsavelNome = unidadeDeSaude.getResponsavel().getNome();
        }
        return new UnidadeSaudeResponseDto(
                unidadeDeSaude.getNome(),
                unidadeDeSaude.getTipo(),
                administracaoSuperior,
                supervisaoRegional,
                unidadeDeSaude.getResponsavelCpf(),
                responsavelNome,
                endereco,
                unidadeDeSaude.getSaudeTelefones(),
                unidadeDeSaude.getEmail(),
                unidadeDeSaude.getHorarioFuncionamento(),
                unidadeDeSaude.getHorarioAtendimento()
        );
    }
}
