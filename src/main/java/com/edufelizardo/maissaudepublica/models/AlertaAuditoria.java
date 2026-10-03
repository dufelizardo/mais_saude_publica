package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Alerta da auditoria (ADR-0096): um padrão suspeito achado na trilha, para quem audita analisar. Não se apaga; termina
 * procedente ou improcedente, com parecer. Guarda só referências (CPF, unidade, paciente) e a contagem, nunca conteúdo.
 */
@Entity
@Table(name = "TB_ALERTA_AUDITORIA", indexes = {
        @Index(name = "ix_alerta_auditoria_status", columnList = "status, detectadoEm"),
        @Index(name = "ix_alerta_auditoria_sujeito", columnList = "tipo, sujeito, ultimoEventoEm")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AlertaAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoAlertaAuditoria tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SeveridadeAlertaAuditoria severidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlertaAuditoria status;

    /** Identifica o episódio e impede alerta repetido (ex.: tipo, CPF e noite). */
    @Column(nullable = false, unique = true, updatable = false, length = 200)
    private String chave;

    /** Quem gerou o padrão: o CPF, ou "IP ..." quando o login recusado não tem CPF. */
    @Column(nullable = false, updatable = false, length = 100)
    private String sujeito;

    @Column(updatable = false)
    private String usuarioCpf;

    @Column(updatable = false)
    private UUID unidadeId;

    @Column(updatable = false)
    private UUID pacienteId;

    @Column(nullable = false, updatable = false)
    private Instant primeiroEventoEm;

    @Column(nullable = false)
    private Instant ultimoEventoEm;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private Instant detectadoEm;

    @Column(nullable = false)
    private Instant atualizadoEm;

    private String analisadoPorCpf;

    private Instant analisadoEm;

    @Column(length = 2000)
    private String parecer;
}
