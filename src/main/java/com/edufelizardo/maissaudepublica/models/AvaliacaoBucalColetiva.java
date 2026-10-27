package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoSaudeBucal;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Resultado da avaliação coletiva de um participante da ação (ADR-0140): o risco em saúde bucal e a necessidade de
 * tratamento, na escala da avaliação individual (SB1). Quem precisa de tratamento vai para a lista a convocar.
 */
@Entity
@Table(name = "TB_AVALIACAO_BUCAL_COLETIVA", uniqueConstraints = @UniqueConstraint(columnNames = {"acao_id", "paciente_id"}),
        indexes = @Index(name = "ix_avaliacao_bucal_coletiva_paciente", columnList = "paciente_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoBucalColetiva implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcaoProgramatica acao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiscoSaudeBucal risco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NecessidadeTratamentoOdontologico necessidadeTratamento;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private Instant registradoEm;

    private String registradoPorCpf;
}
