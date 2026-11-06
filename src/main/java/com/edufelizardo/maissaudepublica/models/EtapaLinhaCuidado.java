package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ReferenciaPrazoEtapa;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEtapaLinhaCuidado;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Etapa da linha de cuidado (ADR-0152): o que precisa acontecer, quantas vezes e em que prazo. Cumpre-se pelo registro
 * do domínio (atendimento, exame liberado, regulação realizada, dose, alta da internação) ou à mão (manual). Etapa que
 * sai da linha é desativada, não apagada: os percursos guardam registros dela.
 */
@Entity
@Table(name = "TB_ETAPA_LINHA_CUIDADO", indexes = @Index(name = "ix_etapa_linha_cuidado_linha", columnList = "linha_id, ordem"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EtapaLinhaCuidado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LinhaDeCuidado linha;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEtapaLinhaCuidado tipo;

    /** Atendimento: a especialidade (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id", referencedColumnName = "uuid")
    private Especialidade especialidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exame_id", referencedColumnName = "uuid")
    private ExameLaboratorial exame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid")
    private ProcedimentoRegulado procedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imunobiologico_id", referencedColumnName = "uuid")
    private Imunobiologico imunobiologico;

    /** Internação: os CIDs aceitos, separados por vírgula, como prefixos (O80,O81,...); vazio, qualquer alta. */
    @Column(length = 200)
    private String cids;

    /** Quantas vezes precisa acontecer (6 consultas). */
    @Column(nullable = false)
    private int quantidade;

    /** Em dias, contados da referência; vazio, sem prazo. */
    private Integer prazoDias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenciaPrazoEtapa prazoDesde;

    @Column(nullable = false)
    private boolean obrigatoria;

    @Column(nullable = false)
    private boolean ativa;
}
