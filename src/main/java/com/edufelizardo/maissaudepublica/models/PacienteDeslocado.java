package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.LocalDeslocamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDeslocamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Paciente fora de casa por causa da emergência (ADR-0193): a origem (o domicílio ou a unidade evacuada), o destino (unidade de saúde,
 * abrigo, casa de parente ou outro) e a situação. A realocação fecha este registro e abre outro ligado a ele, para guardar o caminho.
 * Abrigo não é domínio: é a instituição da rede intersetorial ou um texto.
 */
@Entity
@Table(name = "TB_PACIENTE_DESLOCADO", indexes = {@Index(name = "ix_paciente_deslocado_emergencia", columnList = "emergencia_id, situacao"),
        @Index(name = "ix_paciente_deslocado_paciente", columnList = "paciente_id, situacao")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PacienteDeslocado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private LocalDeslocamento origemTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_unidade_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude origemUnidade;

    @Column(length = 500, updatable = false)
    private String origemDescricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private LocalDeslocamento destinoTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_unidade_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude destinoUnidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_instituicao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstituicaoExterna destinoInstituicao;

    @Column(length = 500, updatable = false)
    private String destinoDescricao;

    @Column(nullable = false, updatable = false)
    private Instant saidaEm;

    @Column(length = 1000)
    private String necessidades;

    @Column(length = 200)
    private String responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoDeslocamento situacao;

    private Instant fimEm;

    @Column(length = 500)
    private String motivoFim;

    /** O registro anterior, quando este veio de uma realocação. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anterior_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PacienteDeslocado anterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_transporte_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SolicitacaoTransporte solicitacaoTransporte;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** O nome do destino para mostrar: a unidade, a instituição ou o texto. */
    public String destinoNome() {
        if (destinoUnidade != null) {
            return destinoUnidade.getNome();
        }
        if (destinoInstituicao != null) {
            return destinoInstituicao.getNome();
        }
        return destinoDescricao;
    }
}
