package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.Endereco;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EnderecoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "O CEP não pode ser branco, vazio ou conter espaços")
    private String cep;
    @NotBlank(message = "O Logradouro não pode ser branco, vazio ou conter espaços")
    private String logradouro;
    @NotBlank(message = "O Número não pode ser branco, vazio ou conter espaços")
    private String numeroLogradouro;
    private String complemento;
    @NotBlank(message = "O Bairro não pode ser branco, vazio ou conter espaços")
    private String bairro;
    @NotBlank(message = "O Cidade não pode ser branco, vazio ou conter espaços")
    private String cidade;
    @NotBlank(message = "O Estado não pode ser branco, vazio ou conter espaços")
    private String estado;
    private String ddd;

    public static EnderecoRequestDto fromEndereco(Endereco endereco) {
        return new EnderecoRequestDto(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumeroLogradouro(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getDdd()
        );
    }

    public static EnderecoRequestDto fromEndereco(EnderecoRequestDto endereco) {
        return new EnderecoRequestDto(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumeroLogradouro(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getDdd()
        );
    }
}
