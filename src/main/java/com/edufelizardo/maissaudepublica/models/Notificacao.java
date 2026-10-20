package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Notificação de agravo (ADR-0124). Notificação não é caso confirmado: segue para investigação, classificação e
 * encerramento. Guarda as respostas da ficha junto com a versão. Dado de saúde (LGPD, art. 11).
 */
@Entity
@Table(name = "TB_NOTIFICACAO", indexes = {@Index(name = "ix_notificacao_paciente", columnList = "paciente_id, agravo_id"), @Index(name = "ix_notificacao_unidade", columnList = "unidade_id, data_notificacao")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Notificacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agravo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Agravo agravo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(updatable = false)
    private LocalDate dataPrimeirosSintomas;

    @Column(nullable = false, updatable = false)
    private LocalDate dataNotificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrigemNotificacao origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", updatable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid", updatable = false)
    private Internacao internacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_exame_id", referencedColumnName = "uuid", updatable = false)
    private PedidoExame pedidoExame;

    /** A crise em saúde mental que originou (ADR-0123). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crise_id", referencedColumnName = "uuid", updatable = false)
    private EventoCrise crise;

    @Column(length = 500, updatable = false)
    private String localProvavel;

    /** As respostas da ficha, em JSON. */
    @Column(columnDefinition = "text", updatable = false)
    private String respostas;

    @Column(updatable = false)
    private Integer fichaVersao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoNotificacao situacao;

    @Column(nullable = false, updatable = false)
    private LocalDate prazoEncerramento;

    @Enumerated(EnumType.STRING)
    private ClassificacaoCasoEpidemiologico classificacaoFinal;

    @Enumerated(EnumType.STRING)
    private EvolucaoCaso evolucao;

    private Instant encerradaEm;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
