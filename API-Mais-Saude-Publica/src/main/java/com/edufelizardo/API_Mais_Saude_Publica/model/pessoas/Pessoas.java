package com.edufelizardo.API_Mais_Saude_Publica.model.pessoas;

//import com.edufelizardo.API_Mais_Saude_Publica.dtos.PessoasDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.model.pessoas.enuns.EstadoCivil;
import com.edufelizardo.API_Mais_Saude_Publica.model.pessoas.enuns.Genero;
import com.edufelizardo.API_Mais_Saude_Publica.model.pessoas.enuns.Nacionalidade;
import jakarta.persistence.Embedded;

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
    @Embedded
    private Endereco endereco;
    private String profissao;
    private byte[] foto;
    private String anotacoes;
//    private DocumentacaoPaciente documentacaoPaciente;

//    public Pessoas(PessoasDto dto) {
//        this.nome = dto.nome();
//        this.dataNascimento = dto.dataNascimento();
//        this.numeroIndentificacao = dto.numeroIndentificacao();
//        this.cpf = dto.cpf();
//        this.cartaoNacionalSaude = dto.cartaoNacionalSaude();
//        this.genero = dto.genero();
//        this.nacionalidade = dto.nacionalidade();
//        this.estadoCivil = dto.estadoCivil();
//        this.enderecoEmail = dto.enderecoEmail();
//        this.telefones = dto.telefones();
//        this.endereco = dto.endereco();
//        this.profissao = dto.profissao();
//        this.foto = dto.foto();
//        this.anotacoes = dto.anotacoes();
//    }
}
