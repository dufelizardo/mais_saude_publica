package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PacienteResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private String cpf;
    private String cartaoSus;
    private LocalDate dataNascimento;
    private Sexo sexo;
    private EnderecoResponseDto endereco;
    private Set<String> telefones;
    private String email;
    private boolean ativo;

    public static PacienteResponseDto fromPaciente(Paciente paciente) {
        return new PacienteResponseDto(
                paciente.getUuid(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getCartaoSus(),
                paciente.getDataNascimento(),
                paciente.getSexo(),
                EnderecoResponseDto.fromEndereco(paciente.getEndereco()),
                paciente.getTelefones(),
                paciente.getEmail(),
                paciente.isAtivo()
        );
    }
}
