package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EnderecoRequestDto;
import jakarta.persistence.Embeddable;
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
public class Endereco implements Serializable {
    @Serial
    private static final long serialVersionUID = 8827247486336275678L;

    private String cep;
    private String logradouro;
    private String numeroLogradouro;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String ddd;

    public Endereco(EnderecoRequestDto dto) {
        this.cep = dto.getCep();
        this.logradouro = dto.getLogradouro();
        this.numeroLogradouro = dto.getNumeroLogradouro();
        this.complemento = dto.getComplemento();
        this.bairro = dto.getBairro();
        this.cidade = dto.getCidade();
        this.estado = dto.getEstado();
        this.ddd = dto.getDdd();
    }
}
