package com.edufelizardo.API_Mais_Saude_Publica.model.pessoas;

import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.model.pessoas.enuns.EstadoCivil;
import com.edufelizardo.API_Mais_Saude_Publica.model.pessoas.enuns.Genero;
import com.edufelizardo.API_Mais_Saude_Publica.model.pessoas.enuns.Nacionalidade;

import java.time.LocalDate;
import java.util.Set;
/**
 * @author Eduardo Felizardo Cândido
 * @version 0.00.00
 * @apiNote Mais Saúde Pública 12/2023
 */
public class Pessoas {
    private String nome;
    private LocalDate dataNascimento;
    private String numeroIndentificacao;
    private String cpf;
    private String cartaoNacionalSaude;
    private Genero genero;
    private Nacionalidade nacionalidade;
    private EstadoCivil estadoCivil;
    private String enderecoEmail;
    private Set<String> telefones;
    private Endereco endereco;
    private String profissao;
    private byte[] foto;
    private String anotacoes;
//    private DocumentacaoPaciente documentacaoPaciente;

}
