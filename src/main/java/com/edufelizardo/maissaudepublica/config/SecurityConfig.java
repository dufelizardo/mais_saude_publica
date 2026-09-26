package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.exceptions.ErrorExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Gate de autenticação por toggle (ver ADR-0055): quando {@code app.security.enabled=false}
 * (default local/CI), o comportamento é idêntico ao que o projeto sempre teve — tudo liberado, sem
 * nenhum impacto nos testes existentes. Quando {@code true} (ambientes implantados, ligado
 * manualmente por ambiente quando estiver pronto — ver ADR-0055), toda rota sob {@code /api/**}
 * exige um JWT válido, exceto {@code /api/v1/auth/**} e a documentação Swagger. Sem RBAC granular
 * ainda: é "autenticado ou não", não "pode fazer X" (Papel/Permissao/EscopoAcesso da ADR-0054
 * ficam para quando houver um consumidor real).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF protege contra requisicoes forjadas que abusam de credencial ambiente (cookie de
        // sessao) enviada automaticamente pelo navegador. Esta API e stateless: autenticacao viaja
        // por Bearer token no header Authorization, nunca por cookie/sessao (ver
        // SessionCreationPolicy.STATELESS logo abaixo) - nao ha credencial ambiente para um site
        // malicioso explorar, entao CSRF nao se aplica. Mesma orientacao oficial do Spring Security
        // para APIs REST stateless (ver ADR-0006/ADR-0055).
        http.csrf(csrf -> csrf.disable()) // lgtm[java/spring-disabled-csrf-protection]
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!securityEnabled) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(this::responderNaoAutenticado))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void responderNaoAutenticado(HttpServletRequest request,
                                          HttpServletResponse response,
                                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorExceptionResponse body = new ErrorExceptionResponse("Unauthorized",
                "Requisição sem autenticação válida.");
        new ObjectMapper().writeValue(response.getWriter(), body);
    }
}
