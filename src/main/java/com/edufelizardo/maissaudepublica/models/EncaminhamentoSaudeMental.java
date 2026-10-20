package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Ligação entre o caso em saúde mental e uma solicitação da Regulação (ADR-0122): a referência e a contrarreferência
 * são as da Regulação (ADRs 0087 a 0089). A Regulação não ganha campo de saúde mental; a ligação fica aqui.
 */
@Entity
@Table(name = "TB_ENCAMINHAMENTO_SAUDE_MENTAL", uniqueConstraints = @UniqueConstraint(name = "uk_encaminhamento_sm_solicitacao", columnNames = {"acompanhamento_id", "solicitacao_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoSaudeMental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private SolicitacaoRegulacao solicitacao;

    @Column(nullable = false, updatable = false)
    private Instant vinculadoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
