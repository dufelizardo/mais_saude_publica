package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Encaminhamento do paciente a uma instituição externa (ADR-0154): o domínio e o registro de origem (o caso em saúde
 * mental, na SM6), o motivo, o prazo esperado de retorno e a resposta da instituição, como uma contrarreferência
 * informal. Cada domínio de origem aplica as próprias regras de acesso; o dado é de saúde.
 */
@Entity
@Table(name = "TB_ENCAMINHAMENTO_INTERSETORIAL", indexes = {
        @Index(name = "ix_encaminhamento_intersetorial_origem", columnList = "dominio_origem, origem_id"),
        @Index(name = "ix_encaminhamento_intersetorial_enviado", columnList = "dominio_origem, enviado_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoIntersetorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private InstituicaoExterna instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DominioOrigemEncaminhamento dominioOrigem;

    /** O registro do domínio de origem (o caso em saúde mental). */
    @Column(nullable = false, updatable = false)
    private UUID origemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, length = 1000, updatable = false)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private LocalDate enviadoEm;

    @Column(nullable = false)
    private LocalDate retornoEsperadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoEncaminhamentoIntersetorial situacao;

    /** Quando a instituição confirmou que recebeu. */
    private LocalDate recebidoEm;

    /** A resposta da instituição, que conclui ou recusa. */
    private LocalDate retornoEm;

    @Column(length = 2000)
    private String retorno;

    private String retornoRegistradoPorCpf;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** Em aberto depois do prazo esperado: sem retorno. */
    public boolean semRetorno(LocalDate hoje) {
        return situacao.emAberto() && retornoEsperadoEm.isBefore(hoje);
    }
}
