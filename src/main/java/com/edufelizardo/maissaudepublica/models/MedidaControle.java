package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaControle;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaControle;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Medida de controle (ADR-0131): de um surto ou de um caso isolado (o bloqueio vacinal de um caso de sarampo não precisa
 * de surto), com o responsável, o prazo, o andamento e o resultado.
 */
@Entity
@Table(name = "TB_MEDIDA_CONTROLE", indexes = {
        @Index(name = "ix_medida_surto", columnList = "surto_id"),
        @Index(name = "ix_medida_notificacao", columnList = "notificacao_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaControle implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surto_id", referencedColumnName = "uuid", updatable = false)
    private Surto surto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", updatable = false)
    private Notificacao notificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMedidaControle tipo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid")
    private Profissional responsavel;

    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoMedidaControle situacao;

    @Column(length = 2000)
    private String resultado;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    private Instant concluidaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
