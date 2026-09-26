package com.edufelizardo.maissaudepublica.services.version1;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Emissão/validação de JWT (mecanismo escolhido pela ADR-0006, modelo de identidade pela
 * ADR-0054). Se {@code app.security.jwt-secret} não estiver configurado, gera uma chave aleatória
 * em memória na subida da aplicação — nenhum segredo fica hardcoded/commitado no git; o
 * custo é que tokens não sobrevivem a um restart do processo até um segredo real ser provisionado
 * via variável de ambiente/Secret do k8s (ver ADR-0055). Isso vale igualmente para local, CI e
 * qualquer ambiente que ainda não tenha provisionado o segredo real — nunca derruba a subida da
 * aplicação.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${app.security.jwt-secret:}")
    private String configuredSecret;

    @Value("${app.security.jwt-expiration-hours:8}")
    private long expirationHours;

    @Value("${app.security.jwt-expiration-hours-remember-me:168}")
    private long expirationHoursRememberMe;

    private SecretKey key;

    @PostConstruct
    void init() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            log.warn("app.security.jwt-secret não configurado — gerando uma chave efêmera em "
                    + "memória. Tokens emitidos não sobrevivem a um restart do processo. "
                    + "Configure um segredo real via variável de ambiente antes de depender de "
                    + "sessões persistentes.");
            this.key = Jwts.SIG.HS256.key().build();
        } else {
            this.key = Keys.hmacShaKeyFor(configuredSecret.getBytes());
        }
    }

    public String gerarToken(String cpf, String nome, boolean manterConectado) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(Duration.ofHours(
                manterConectado ? expirationHoursRememberMe : expirationHours));

        return Jwts.builder()
                .subject(cpf)
                .claim("nome", nome)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(key)
                .compact();
    }

    public Instant extrairExpiracao(String token) {
        return extrairClaims(token).getExpiration().toInstant();
    }

    public String extrairCpf(String token) {
        return extrairClaims(token).getSubject();
    }

    /**
     * @return o cpf (subject) do token se válido, ou {@code null} se inválido/expirado/malformado —
     *     o filtro decide o que fazer com um {@code null} (não autenticar a requisição).
     */
    public String validarESubject(String token) {
        try {
            return extrairCpf(token);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
