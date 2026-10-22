package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PapelRedeApoio;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Pessoa ou serviço da rede de apoio do caso (ADR-0121). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PessoaRedeApoioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private PapelRedeApoio papel;
    private String vinculo;
    private String telefone;
    private String observacao;
    private LocalDate inicio;
    private LocalDate fim;
    private String motivoFim;
}
