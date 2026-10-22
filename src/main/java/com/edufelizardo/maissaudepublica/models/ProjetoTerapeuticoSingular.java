package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeto Terapêutico Singular de um caso em saúde mental (ADR-0121, desenho na ADR-0112): uma versão por linha.
 * Revisar cria a versão seguinte e marca esta como substituída; nada é sobrescrito. Os responsáveis são a referência do
 * caso (SM1), não duplicada aqui.
 */
@Entity
@Table(name = "TB_PROJETO_TERAPEUTICO_SINGULAR", uniqueConstraints = @UniqueConstraint(name = "uk_pts_versao", columnNames = {"acompanhamento_id", "versao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProjetoTerapeuticoSingular implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @Column(nullable = false, updatable = false)
    private int versao;

    @Column(nullable = false, length = 4000, updatable = false)
    private String necessidades;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PeriodicidadeAcompanhamento periodicidadeRevisao;

    @Column(nullable = false, updatable = false)
    private LocalDate revisarEm;

    @Column(nullable = false, updatable = false)
    private boolean pactuadoComPessoa;

    @Column(length = 1000, updatable = false)
    private String pactuacaoObservacao;

    /** O que mudou em relação à versão anterior; nula na primeira. */
    @Column(length = 2000, updatable = false)
    private String sinteseRevisao;

    @Column(nullable = false, updatable = false)
    private Instant elaboradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** Quando a versão seguinte a substituiu; nula na vigente. */
    private Instant substituidoEm;

    public boolean vigente() {
        return substituidoEm == null;
    }
}
