package br.com.eduafelizardo.mais_saude_publica.domain;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Endereco {
    private String cep;
    private String logradouro;
    private String complemento;
    private Integer unidade;
    private String bairro;
    private String localidade;
    private String uf;
    private String estado;
    private String regiao;
    private String ibge;
}
