package br.com.eduafelizardo.mais_saude_publica.domain;

import br.com.eduafelizardo.mais_saude_publica.domain.dto.EnderecoRecord;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Endereco {
    private String cep;
    private String logradouro;
    private String complemento;
    private String unidade;
    private String bairro;
    private String localidade;
    private String uf;
    private String estado;
    private String regiao;
    private String ibge;

    public Endereco(EnderecoRecord enderecoRecord) {
        this.cep = enderecoRecord.cep();
        this.logradouro = enderecoRecord.logradouro();
        this.complemento = enderecoRecord.complemento();
        this.unidade = enderecoRecord.unidade();
        this.bairro = enderecoRecord.bairro();
        this.localidade = enderecoRecord.localidade();
        this.uf = enderecoRecord.uf();
        this.estado = enderecoRecord.estado();
        this.regiao = enderecoRecord.regiao();
        this.ibge = enderecoRecord.ibge();
    }
}
