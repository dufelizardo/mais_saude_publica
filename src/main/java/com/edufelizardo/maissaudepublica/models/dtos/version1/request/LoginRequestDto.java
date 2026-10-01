package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoIdentificadorLogin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Corpo do {@code POST /api/v1/auth/login}. {@code tipo} decide como {@code identificador} é
 * resolvido: {@code CPF} busca {@code Usuario} diretamente; {@code MATRICULA} resolve primeiro via
 * {@code Profissional.matricula} para achar o CPF correspondente (ver ADR-0055).
 */
@Data
@Getter
@Setter
public class LoginRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Campo tipo precisa ser preenchido (CPF ou MATRICULA)")
    private TipoIdentificadorLogin tipo;

    @NotBlank
    private String identificador;

    @NotBlank
    private String senha;

    /**
     * Controla, no frontend, se o token é guardado em armazenamento persistente ou só da sessão —
     * o backend usa para decidir o tempo de expiração do JWT emitido (ver ADR-0055).
     */
    private boolean manterConectado;
}
