package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoAcessoJustificado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Acesso ao prontuário sem vínculo assistencial, declarado por quem acessa (ADR-0076): motivo, texto
 * livre e validade curta, só para aquele paciente. O texto fica aqui, e não na trilha de auditoria, que
 * não guarda conteúdo (ADR-0070); o evento de auditoria aponta para este registro.
 *
 * <p>Imutável: não se altera nem se apaga.
 */
@Entity
@Immutable
@Table(name = "TB_ACESSO_JUSTIFICADO", indexes = {
        @Index(name = "ix_acesso_justificado_usuario_paciente", columnList = "usuarioCpf, paciente_uuid, expiraEm")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcessoJustificado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, updatable = false)
    private String usuarioCpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MotivoAcessoJustificado motivo;

    @Column(nullable = false, updatable = false, length = 1000)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private Instant concedidoEm;

    @Column(nullable = false, updatable = false)
    private Instant expiraEm;
}
