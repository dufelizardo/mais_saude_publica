package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMobilizacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Mobilização de recurso na emergência (ADR-0158): Emergências registra, o dono executa. O profissional mobilizado pode ter turno na unidade de destino sem lotação lá (exceção da Escala); o medicamento pode ligar-se a uma transferência da Farmácia. */
@Entity
@Table(name = "TB_MOBILIZACAO", indexes = {@Index(name = "ix_mobilizacao_emergencia", columnList = "emergencia_id, tipo"), @Index(name = "ix_mobilizacao_profissional", columnList = "profissional_id, destino_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Mobilizacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoMobilizacao tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origem_id", referencedColumnName = "uuid", updatable = false)
    private UnidadeDeSaude origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude destino;

    /** O profissional mobilizado (tipo PROFISSIONAL). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, length = 300, updatable = false)
    private String recurso;

    @Column(updatable = false)
    private Integer quantidade;

    @Column(length = 200)
    private String responsavel;

    @Column(nullable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoMobilizacao situacao;

    /** A transferência da Farmácia que executa a mobilização de medicamento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", referencedColumnName = "uuid")
    private TransferenciaFarmacia transferencia;

    /** A capacidade de leitos ativada que a mobilização de leito usa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacidade_ativacao_id", referencedColumnName = "uuid")
    private AtivacaoCapacidade capacidadeAtivacao;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
