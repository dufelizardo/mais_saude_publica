package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Profissional;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProfissionalResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String matricula;
    private String cpf;
    private String nome;
    private String conselhoClasse;
    private String numeroConselho;
    private EnderecoResponseDto endereco;
    private Set<String> telefones;
    private String email;
    private LocalDate dataAdmissao;
    private LocalDate dataDesligamento;
    private boolean ativo;

    public static ProfissionalResponseDto fromProfissional(Profissional profissional) {
        return new ProfissionalResponseDto(
                profissional.getMatricula(),
                profissional.getCpf(),
                profissional.getNome(),
                profissional.getConselhoClasse(),
                profissional.getNumeroConselho(),
                EnderecoResponseDto.fromEndereco(profissional.getEndereco()),
                profissional.getTelefones(),
                profissional.getEmail(),
                profissional.getDataAdmissao(),
                profissional.getDataDesligamento(),
                profissional.isAtivo()
        );
    }
}
