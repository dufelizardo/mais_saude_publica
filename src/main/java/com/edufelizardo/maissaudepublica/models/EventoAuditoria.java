package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Quem fez o quê, quando, em qual registro, de qual paciente e em qual unidade (ADR-0054 item 6, ADR-0070).
 * Imutável: não se altera nem se apaga — a trilha só cresce. Sem conteúdo da requisição (nem senha, nem
 * texto clínico): o registro de negócio já guarda o conteúdo e o histórico de retificação.
 */
@Entity
@Immutable
@Table(name = "TB_EVENTO_AUDITORIA", indexes = {
        @Index(name = "ix_auditoria_paciente", columnList = "pacienteId, ocorridoEm"),
        @Index(name = "ix_auditoria_usuario", columnList = "usuarioCpf, ocorridoEm"),
        @Index(name = "ix_auditoria_registro", columnList = "registroId"),
        // Detecção de alertas (ADR-0096): agrega por ação e período.
        @Index(name = "ix_auditoria_acao_data", columnList = "acao, ocorridoEm")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoAuditoria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    /** Nulo sem login (toggle desligado) — ou, no login recusado, o CPF tentado. */
    @Column(updatable = false)
    private String usuarioCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AcaoAuditoria acao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ResultadoAuditoria resultado;

    /** Recurso da API (ex.: TRIAGEM), derivado do controller. */
    @Column(nullable = false, updatable = false)
    private String recurso;

    @Column(nullable = false, updatable = false)
    private String metodo;

    /** Rota como padrão (ex.: /api/v1/triagem/{uuid}/retificacao), sem parâmetros. */
    @Column(nullable = false, updatable = false)
    private String rota;

    @Column(updatable = false)
    private int statusHttp;

    @Column(updatable = false)
    private UUID registroId;

    @Column(updatable = false)
    private UUID pacienteId;

    @Column(updatable = false)
    private UUID unidadeId;

    @Column(updatable = false)
    private String origemIp;

    /** Motivo da recusa (mensagem do 403), quando houver. */
    @Column(length = 500, updatable = false)
    private String detalhe;
}
