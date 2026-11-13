package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoAtingido;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMetaComunicacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A avaliação registrada da campanha de comunicação encerrada (C6, ADR-0196): o julgamento da equipe e os números da apuração
 * congelados no momento do registro, para o julgamento não mudar se alguém mexer numa ação depois.
 */
@Entity
@Table(name = "TB_AVALIACAO_CAMPANHA", uniqueConstraints = @UniqueConstraint(name = "uk_avaliacao_campanha_campanha", columnNames = "campanha_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoCampanha implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CampanhaComunicacao campanha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PublicoAtingido publicoAtingido;

    @Column(length = 2000)
    private String resultados;

    @Column(nullable = false, length = 2000)
    private String conclusoes;

    @Column(length = 2000)
    private String recomendacoes;

    // ── Números congelados ─────────────────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private int pessoasAlcancadas;

    @Column(nullable = false)
    private int acoesRealizadas;

    @Column(nullable = false)
    private int enviados;

    @Column(precision = 5, scale = 1)
    private BigDecimal coberturaPercentual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SituacaoMetaComunicacao situacaoMetaPessoas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SituacaoMetaComunicacao situacaoMetaAcoes;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradaPorCpf;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private String atualizadaPorCpf;
}
