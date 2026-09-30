package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Quem está logado (ADR-0065): o usuário e, quando existe, o profissional ativo com o mesmo CPF — o
 * vínculo fraco por CPF entre identidade de login e registro funcional (ADR-0055, ADR-0014). As telas
 * usam a matrícula para preencher o profissional dos registros.
 *
 * <p>Traz também os acessos vigentes e as permissões efetivas (ADR-0066), para a interface mostrar só o
 * que o usuário pode usar.
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
    /** Papéis vigentes com o escopo de cada um. */
    private List<AcessoVigente> acessos;
    /** União das permissões dos papéis vigentes (qualquer escopo), em ordem alfabética. */
    private List<String> permissoes;
    /** Senha provisória pendente de troca (ADR-0069). */
    private boolean trocarSenha;

    /** Um papel vigente num escopo; unidade nula = rede inteira. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class AcessoVigente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String papelCodigo;
        private String papelNome;
        private UUID unidadeUuid;
        private String unidadeNome;
    }
}
