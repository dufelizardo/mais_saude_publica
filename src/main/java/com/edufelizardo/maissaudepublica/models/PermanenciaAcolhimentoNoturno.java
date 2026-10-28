package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A permanência do paciente no acolhimento noturno do CAPS (ADR-0153): a entrada, a saída prevista (além da
 * permanência máxima, com a justificativa), a saída e o motivo. Dado de saúde, do caso em saúde mental.
 */
@Entity
@Table(name = "TB_PERMANENCIA_ACOLHIMENTO_NOTURNO", indexes = {@Index(name = "ix_permanencia_acolhimento_unidade", columnList = "unidade_id, saida"), @Index(name = "ix_permanencia_acolhimento_paciente", columnList = "paciente_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PermanenciaAcolhimentoNoturno implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @Column(nullable = false, updatable = false)
    private LocalDate entrada;

    @Column(nullable = false)
    private LocalDate saidaPrevista;

    @Column(length = 500)
    private String justificativaPrazo;

    private LocalDate saida;

    @Column(length = 500)
    private String motivoSaida;

    @Column(nullable = false, updatable = false)
    private Instant registradaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
