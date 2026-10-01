package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Só roda quando {@code app.security.enabled=true} (ver {@link SecurityConfig}, que só registra
 * este filtro nesse caso). Marca a requisição como autenticada; a permissão é decidida depois
 * (ADR-0067).
 *
 * <p>Além da assinatura e da validade, confere o usuário no banco (ADR-0078): ele precisa existir, estar
 * ativo e ter a mesma versão de sessões do token. Assim, desativar, redefinir a senha ou encerrar as
 * sessões vale na hora, sem esperar o token expirar. Token sem a versão (emitido antes) conta como 0.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            Claims claims = jwtService.validar(token);
            String cpf = claims != null ? claims.getSubject() : null;
            if (cpf != null && SecurityContextHolder.getContext().getAuthentication() == null && sessaoValida(cpf, claims)) {
                // Senha provisória (ADR-0069): a sessão só serve para trocar a senha.
                var autoridades = Boolean.TRUE.equals(claims.get(JwtService.CLAIM_TROCAR_SENHA, Boolean.class))
                        ? List.of(new SimpleGrantedAuthority(UsuarioAutenticado.SENHA_PROVISORIA))
                        : List.<SimpleGrantedAuthority>of();
                var authentication = new UsernamePasswordAuthenticationToken(cpf, null, autoridades);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean sessaoValida(String cpf, Claims claims) {
        Integer versao = claims.get(JwtService.CLAIM_VERSAO_SESSAO, Integer.class);
        int versaoDoToken = versao != null ? versao : 0;
        return usuarioRepository.findByCpf(cpf)
                .filter(Usuario::isAtivo)
                .map(u -> u.getVersaoSessao() == versaoDoToken)
                .orElse(false);
    }
}
