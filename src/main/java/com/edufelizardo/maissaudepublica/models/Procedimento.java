package com.edufelizardo.maissaudepublica.models;

import java.time.Instant;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Procedimento realizado durante uma {@link Consulta} — quinta e última entidade da onda "Operação
 * Assistencial" antes do endpoint de agregação do Prontuário (ver ADR-0039, MAPA-DE-DOMINIOS.md #6,
 * ADR-0044). {@code tipo} é texto livre, não um enum fechado — o desenho original do DER.md já
 * descrevia um conjunto aberto ("CIRURGIA, EXAME, etc."), diferente de {@code Atendimento.tipo}/
 * {@code Agendamento.tipo}, que têm listas fechadas. {@code profissional} referencia
 * {@link Profissional} por FK direta (uuid interno), resolvida a partir da matrícula na fronteira
 * da API — mesmo padrão de {@code Consulta.profissional} (ADR-0034/ADR-0043).
 */
@Entity
@Table(name = "TB_PROCEDIMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Procedimento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", referencedColumnName = "uuid", nullable = false)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotBlank
    private String tipo;

    private String descricao;

    @NotNull
    private LocalDateTime dataRealizacao;

    @Enumerated(EnumType.STRING)
    private StatusProcedimento status;

    /**
     * Registro que esta versão corrige (ADR-0062). Nulo no registro original. Registros clínicos não
     * são editados: uma correção é uma nova versão ligada à anterior, que continua no prontuário.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private Procedimento retificacaoDe;

    @Column(length = 1000, updatable = false)
    private String motivoRetificacao;

    /** Hora do servidor em que o registro foi gravado (ADR-0062); nula em registros anteriores. */
    @Column(updatable = false)
    private Instant registradoEm;

    /** CPF do usuário autenticado quando o toggle de segurança está ligado (ADR-0055). */
    @Column(updatable = false)
    private String registradoPorCpf;

    // ── Mudança de status (ADR-0062): AGENDADO → REALIZADO | CANCELADO, uma única vez ────────────

    /** Data que estava prevista enquanto AGENDADO; guardada quando o procedimento é realizado ou cancelado. */
    private LocalDateTime dataPrevista;

    private Instant statusAlteradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_status_id", referencedColumnName = "uuid")
    private Profissional profissionalStatus;

    @Column(length = 1000)
    private String justificativaStatus;

    private String statusAlteradoPorCpf;

    public Procedimento(Consulta consulta, Profissional profissional, String tipo, String descricao,
                         LocalDateTime dataRealizacao, StatusProcedimento status) {
        this.consulta = consulta;
        this.profissional = profissional;
        this.tipo = tipo;
        this.descricao = descricao;
        this.dataRealizacao = dataRealizacao;
        this.status = status;
    }
}
