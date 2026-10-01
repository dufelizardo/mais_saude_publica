package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Checagem de enfermagem de um medicamento prescrito — terceira entidade do domínio Enfermagem
 * (MAPA-DE-DOMINIOS.md #8, ADR-0064), que liga a Enfermagem à Farmácia. Nasce da {@link Consulta}
 * vigente do atendimento (a prescrição) e registra se o medicamento foi administrado — com lote, dose,
 * via e quantidade, baixando o lote pelo livro da Farmácia (ADR-0057) — ou não, com o motivo.
 *
 * <p>Registro clínico: não é editado; correção é retificação (ADR-0062), que estorna a baixa anterior.
 */
@Entity
@Table(name = "TB_ADMINISTRACAO_MEDICAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AdministracaoMedicamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Atendimento atendimento;

    /** Consulta com a prescrição que originou a administração. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Medicamento medicamento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private SituacaoAdministracao situacao;

    /** Lote de onde saiu a quantidade; nulo quando não administrado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", updatable = false)
    private Lote lote;

    /** Dose como prescrita e dada (ex.: "1 g", "500 mg", "10 gotas"). */
    @Column(updatable = false)
    private String dose;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private ViaAdministracao via;

    /** Unidades retiradas do lote (ampolas, comprimidos, frascos). */
    @Column(updatable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private MotivoNaoAdministracao motivoNaoAdministracao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    /** Hora em que foi administrado (ou em que se constatou que não seria). */
    @NotNull
    @Column(updatable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    // Retificação (ADR-0062)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private AdministracaoMedicamento retificacaoDe;

    @Column(length = 1000, updatable = false)
    private String motivoRetificacao;

    @Column(updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
