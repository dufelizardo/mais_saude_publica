package com.edufelizardo.API_Mais_Saude_Publica.dtos;

public record EnderecoDto(
        String cep,
        String logradouro,
        Integer numeroLogradouro,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        long ibge,
        long gia,
        Integer ddd,
        Integer siafi
) {
}
