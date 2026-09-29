package com.edufelizardo.maissaudepublica.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CPF do usuário autenticado, gravado nos registros auditáveis (livro da Farmácia, registros
 * clínicos). O principal é colocado pelo JwtAuthenticationFilter; com o toggle de segurança
 * desligado (ADR-0055) a requisição é anônima e o CPF é nulo.
 */
public final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    public static String cpf() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof UsernamePasswordAuthenticationToken && autenticacao.getPrincipal() instanceof String cpf) {
            return cpf;
        }
        return null;
    }
}
