package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoExterna;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Instituição externa do catálogo da Intersetorialidade (ADR-0154). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InstituicaoExternaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private SetorIntersetorial setor;
    private TipoInstituicaoExterna tipo;
    private String endereco;
    private String telefone;
    private String email;
    private String responsavel;
    private String observacao;
    private boolean ativa;
    private Instant atualizadaEm;
}
