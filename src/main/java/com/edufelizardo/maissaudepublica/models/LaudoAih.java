package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.CausaExternaAih;
import com.edufelizardo.maissaudepublica.models.enuns.ClinicaAih;
import com.edufelizardo.maissaudepublica.models.enuns.StatusLaudoAih;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Laudo para solicitação de AIH (ADR-0186): a justificativa, o diagnóstico e o procedimento da internação, que o médico autorizador
 * autoriza (com o número da AIH tirado da faixa vigente), devolve para correção ou nega. Pertence a uma internação ou, antes da
 * admissão, a uma internação eletiva agendada (ADR-0185); na admissão, passa para a internação.
 */
@Entity
@Table(name = "TB_LAUDO_AIH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LaudoAih implements Serializable {
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
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid")
    private Internacao internacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", referencedColumnName = "uuid", updatable = false)
    private InternacaoAgendada agendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaraterInternacao carater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", referencedColumnName = "uuid", nullable = false)
    private Profissional solicitante;

    @Column(nullable = false, length = 2000)
    private String sinaisSintomas;

    @Column(nullable = false, length = 2000)
    private String condicoesJustificativas;

    @Column(length = 2000)
    private String resultadosExames;

    @Column(nullable = false, length = 500)
    private String diagnosticoInicial;

    @Column(nullable = false, length = 10)
    private String cidPrincipal;

    @Column(length = 10)
    private String cidSecundario;

    @Column(length = 10)
    private String cidCausaAssociada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid", nullable = false)
    private ProcedimentoCatalogo procedimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClinicaAih clinica;

    @Enumerated(EnumType.STRING)
    private CausaExternaAih causaExterna;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLaudoAih status;

    @NotNull
    @Column(nullable = false)
    private Instant solicitadoEm;

    /** Número da AIH (13 dígitos, com o dígito verificador), dado na autorização. */
    @Column(length = 13, unique = true)
    private String numeroAih;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizador_id", referencedColumnName = "uuid")
    private Profissional autorizador;

    private Instant decididoEm;

    @Column(length = 1000)
    private String motivoDecisao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
