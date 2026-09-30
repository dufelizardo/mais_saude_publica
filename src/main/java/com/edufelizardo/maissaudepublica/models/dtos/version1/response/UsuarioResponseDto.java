package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Usuario;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Usuário sem dado sensível (nunca o hash da senha). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UsuarioResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String cpf;
    private String nome;
    private boolean ativo;
    private Instant bloqueadoAte;
    private Instant ultimoAcessoEm;

    public static UsuarioResponseDto fromUsuario(Usuario u) {
        return new UsuarioResponseDto(u.getUuid(), u.getCpf(), u.getNome(), u.isAtivo(), u.getBloqueadoAte(), u.getUltimoAcessoEm());
    }
}
