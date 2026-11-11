package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRequisicaoEstoque;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Requisição de materiais de uma unidade ao almoxarifado central (ADR-0189): rascunho, enviada, atendida (com a transferência),
 * recebida, negada ou cancelada.
 */
@Entity
@Table(name = "TB_REQUISICAO_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RequisicaoEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "central_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude central;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeRequisicaoEstoque prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoRequisicaoEstoque situacao;

    @Column(length = 1000)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", referencedColumnName = "uuid")
    private Profissional solicitante;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    private Instant enviadaEm;

    private Instant decididaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id", referencedColumnName = "uuid")
    private Profissional atendente;

    @Column(length = 1000)
    private String motivo;

    /** A transferência gerada pelo atendimento. */
    private UUID transferenciaId;

    /** A emergência que gerou a requisição urgente (ES3, ADR-0191); o atendimento pode usar a reserva dela na central. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Emergencia emergencia;

    private Instant recebidaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
