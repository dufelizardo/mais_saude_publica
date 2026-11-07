package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Canal de comunicação (ADR-0168). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CanalComunicacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoCanalComunicacao tipo;
    private String nome;
    private String descricao;
    private boolean ativo;
}
