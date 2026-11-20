package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ordem de serviço de manutenção (ADR-0216): do chamado corretivo ou do plano vencido; triagem, execução interna ou por contrato,
 * conclusão com o serviço, as peças e o custo. */
@Entity
@Table(name = "TB_ORDEM_SERVICO_MANUTENCAO", indexes = {@Index(name = "ix_ordem_servico_bem", columnList = "bem_id, situacao"), @Index(name = "ix_ordem_servico_plano", columnList = "plano_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OrdemServicoManutencao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bem_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BemPatrimonial bem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PlanoManutencao plano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoManutencao tipo;

    /** O defeito relatado no chamado; vazio na ordem do plano. */
    @Column(length = 1000)
    private String defeito;

    @Column(nullable = false)
    private boolean bemParado;

    /** Desde quando o bem está parado: a abertura do chamado com o bem parado, ou o início da execução. */
    private LocalDate paradoDesde;

    @Column(nullable = false, updatable = false)
    private LocalDate abertaEm;

    @Column(updatable = false)
    private String abertaPorCpf;

    @Enumerated(EnumType.STRING)
    private PrioridadeManutencao prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoOrdemServico situacao;

    @Enumerated(EnumType.STRING)
    private ExecucaoManutencao execucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstrumentoContratual instrumento;

    @Column(length = 150)
    private String tecnico;

    /** A situação do bem antes da execução, devolvida na conclusão ou no cancelamento. */
    @Enumerated(EnumType.STRING)
    private SituacaoBem situacaoBemAnterior;

    private LocalDate iniciadaEm;

    private LocalDate concluidaEm;

    @Column(length = 1000)
    private String servicoRealizado;

    @Column(precision = 15, scale = 2)
    private BigDecimal custoMaoDeObra;

    @Column(length = 60)
    private String certificado;

    private LocalDate calibracaoValidaAte;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
