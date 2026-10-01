package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Link de recuperação de senha (ADR-0081). Guarda só o hash (SHA-256) do token que foi por e-mail: quem lê o
 * banco não consegue usar o link. Vale uma vez e por pouco tempo; pedir outro invalida os anteriores.
 */
@Entity
@Table(name = "TB_REDEFINICAO_SENHA", indexes = {
        @Index(name = "ix_redefinicao_senha_token", columnList = "tokenHash", unique = true),
        @Index(name = "ix_redefinicao_senha_usuario", columnList = "usuario_uuid, criadaEm")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RedefinicaoSenha implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(nullable = false, updatable = false, length = 64)
    @ToString.Exclude
    private String tokenHash;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(nullable = false, updatable = false)
    private Instant expiraEm;

    /** Quando foi usado para trocar a senha, ou invalidado por um pedido mais novo. Nulo = ainda válido. */
    private Instant usadaEm;

    public boolean valida(Instant agora) {
        return usadaEm == null && expiraEm.isAfter(agora);
    }
}
