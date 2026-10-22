package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ArcadaDentaria;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaProtetica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTratamentoProtetico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProtese;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tratamento protético (ADR-0139, desenho na ADR-0111): a prótese tem um ciclo (planejamento, moldagem, prova,
 * laboratório, entrega, acompanhamento), e não é um procedimento só. O laboratório é uma unidade com a capacidade LRPD
 * ou um laboratório contratado.
 */
@Entity
@Table(name = "TB_TRATAMENTO_PROTETICO", indexes = @Index(name = "ix_tratamento_protetico_paciente", columnList = "paciente_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TratamentoProtetico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Atendimento atendimento;

    /** O plano de tratamento aberto do paciente na abertura, se houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", referencedColumnName = "uuid", updatable = false)
    private PlanoTratamento plano;

    /** O cirurgião-dentista responsável. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoProtese tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ArcadaDentaria arcada;

    /** Na unitária, o dente (FDI). */
    @Column(updatable = false)
    private Integer dente;

    /** O laboratório da rede: unidade com a capacidade LRPD. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_unidade_id", referencedColumnName = "uuid")
    private UnidadeDeSaude laboratorioUnidade;

    /** Ou o laboratório contratado, pelo nome. */
    @Column(length = 200)
    private String laboratorioExterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtapaProtetica etapaAtual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoTratamentoProtetico situacao;

    private LocalDate dataEntrega;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant abertoEm;

    @Column(updatable = false)
    private String abertoPorCpf;

    private Instant encerradoEm;

    private String encerradoPorCpf;

    @Column(length = 500)
    private String motivoCancelamento;
}
