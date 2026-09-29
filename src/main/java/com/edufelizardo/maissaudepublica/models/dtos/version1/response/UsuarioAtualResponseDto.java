package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Quem está logado (ADR-0065): o usuário e, quando existe, o profissional ativo com o mesmo CPF — o
 * vínculo fraco por CPF entre identidade de login e registro funcional (ADR-0055, ADR-0014). As telas
 * usam a matrícula para preencher o profissional dos registros.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UsuarioAtualResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cpf;
    private String nome;
    /** Nulos quando o usuário não tem vínculo ativo como profissional (ex.: administrador da plataforma). */
    private UUID profissionalUuid;
    private String profissionalMatricula;
    private String profissionalNome;
}
