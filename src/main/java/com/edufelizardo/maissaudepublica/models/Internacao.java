package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlta;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Internação de um paciente num leito (ADR-0098). Uma ativa por paciente. O leito muda na troca; a alta fecha a internação
 * com tipo, data e sumário, e não muda mais.
 */
@Entity
@Table(name = "TB_INTERNACAO", indexes = {
        @Index(name = "ix_internacao_paciente", columnList = "paciente_id, status"),
        @Index(name = "ix_internacao_unidade_status", columnList = "unidade_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Internacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", updatable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_id", referencedColumnName = "uuid", nullable = false)
    private Leito leito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_responsavel_id", referencedColumnName = "uuid", nullable = false)
    private Profissional medicoResponsavel;

    @Column(nullable = false, length = 10, updatable = false)
    private String cid;

    @Column(nullable = false, length = 1000, updatable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CaraterInternacao carater;

    @Column(nullable = false, updatable = false)
    private Instant admitidaEm;

    private LocalDate previsaoAlta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusInternacao status;

    private Instant altaEm;

    @Enumerated(EnumType.STRING)
    private TipoAlta tipoAlta;

    @Column(length = 4000)
    private String sumarioAlta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alta_por_id", referencedColumnName = "uuid")
    private Profissional altaPor;

    @Column(updatable = false)
    private String registradoPorCpf;
}
