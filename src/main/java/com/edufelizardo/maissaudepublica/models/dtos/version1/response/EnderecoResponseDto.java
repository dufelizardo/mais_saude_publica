package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Endereco;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class EnderecoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cep;
    private String logradouro;
    private String numeroLogradouro;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String ddd;

    public static EnderecoResponseDto fromEndereco(Endereco endereco) {
        return new EnderecoResponseDto(
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
