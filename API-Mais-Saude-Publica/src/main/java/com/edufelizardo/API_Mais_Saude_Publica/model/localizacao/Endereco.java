package com.edufelizardo.API_Mais_Saude_Publica.model.localizacao;

//import com.edufelizardo.API_Mais_Saude_Publica.dtos.EnderecoDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.EnderecoDto;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * @author Eduardo Felizardo Cândido
 * @version 0.00.00
 * @apiNote Mais Saúde Pública 12/2023
 * @implNote Represents an address.
 * @implNote This class represents an address with various properties such as street, number, city, etc.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Endereco {
    private String cep;
    private String logradouro;
    private String numeroLogradouro;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String ddd;

    public Endereco(EnderecoDto dto) {
        this.cep = dto.cep();
        this.logradouro = dto.logradouro();
        this.numeroLogradouro = dto.numeroLogradouro();
        this.complemento = dto.complemento();
        this.bairro = dto.bairro();
        this.cidade = dto.cidade();
        this.estado = dto.estado();
        this.ddd = dto.ddd();
    }
}
