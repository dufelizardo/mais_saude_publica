package com.edufelizardo.API_Mais_Saude_Publica.dtos;

public record EnderecoDto(
        String cep,
        String logradouro,
        String numeroLogradouro,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String ddd
) {
}
