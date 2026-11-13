package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoOcupacional;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoOcupacional;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** O risco ocupacional de um empregador (ADR-0208): tipo, agente, expostos; ativo ou controlado; pode nascer de uma investigação. */
@Entity
@Table(name = "TB_RISCO_OCUPACIONAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RiscoOcupacional implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoRiscoOcupacional tipo;

    @Column(nullable = false, length = 200, updatable = false)
    private String agente;

    @Column(length = 1000)
    private String descricao;

    @Column(length = 14)
    private String empregadorDocumento;

    @Column(nullable = false, length = 200)
    private String empregadorNome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @Column(length = 500)
    private String local;

    private Integer trabalhadoresExpostos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoRiscoOcupacional situacao;

    private Instant controladoEm;

    @Column(length = 1000)
    private String motivoControle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigacao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InvestigacaoTrabalho investigacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
