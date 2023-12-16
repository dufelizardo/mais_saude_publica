package com.edufelizardo.API_Mais_Saude_Publica.dtos.responses;

import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;

public record EnderecoResponseDto(
        String cep,
        String logradouro,
        Integer numeroLogradouro,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        Integer ddd
) {
    public static EnderecoResponseDto fromEdereco(Endereco endereco){
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
