package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Avaliação odontológica (ADR-0134, desenho na ADR-0111), ligada a um atendimento de especialidade odontológica:
 * queixa, anamnese, exame, diagnóstico, risco em saúde bucal, necessidade de tratamento e o plano inicial. Registro
 * clínico imutável: a correção é uma nova versão ligada à anterior (ADR-0062).
 */
@Entity
@Table(name = "TB_AVALIACAO_ODONTOLOGICA", indexes = @Index(name = "ix_avaliacao_odonto_atendimento", columnList = "atendimento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoOdontologica implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Atendimento atendimento;

    /** O cirurgião-dentista. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(length = 1000, updatable = false)
    private String queixaPrincipal;

    @Column(length = 4000, updatable = false)
    private String anamnese;

    /** Exame clínico e achados (tecidos moles, dentes, periodonto, oclusão). */
    @Column(length = 4000, updatable = false)
    private String exameClinico;

    /** Diagnóstico e condições bucais. */
    @Column(length = 2000, updatable = false)
    private String diagnostico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private RiscoSaudeBucal risco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NecessidadeTratamentoOdontologico necessidadeTratamento;

    @Column(length = 2000, updatable = false)
    private String planoInicial;

    @Column(length = 2000, updatable = false)
    private String observacoes;

    /** Retificação (ADR-0062): a versão que esta corrige. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private AvaliacaoOdontologica retificacaoDe;

    @Column(length = 500, updatable = false)
    private String motivoRetificacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
