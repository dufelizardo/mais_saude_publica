package com.edufelizardo.maissaudepublica.models;

import java.time.Instant;
import com.edufelizardo.maissaudepublica.models.enuns.TipoConsulta;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consulta realizada durante um {@link Atendimento} — quarta entidade da onda "Operação
 * Assistencial" (ver ADR-0039, MAPA-DE-DOMINIOS.md #6, ADR-0043). Mantida como no desenho original
 * do DER.md (campo a campo): {@code diagnostico}, {@code receituario} e {@code examesSolicitados}
 * continuam como campos de texto — sem entidades {@code Diagnostico}/{@code Exame}/
 * {@code Prescricao} próprias por ora (YAGNI: nenhum requisito concreto pede consulta estruturada
 * desses dados ainda; revisitar quando Farmácia/Laboratório entrarem no roadmap). {@code
 * profissional} referencia {@link Profissional} por FK direta (uuid interno), resolvida a partir da
 * matrícula na fronteira da API — mesmo padrão de {@code Atendimento.profissional} (ADR-0034/
 * ADR-0041).
 */
@Entity
@Table(name = "TB_CONSULTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Consulta implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private TipoConsulta tipoConsulta;

    private String queixaPrincipal;
    private String diagnostico;
    private String receituario;
    private String examesSolicitados;
    private LocalDate retorno;

    /**
     * Registro que esta versão corrige (ADR-0062). Nulo no registro original. Registros clínicos não
     * são editados: uma correção é uma nova versão ligada à anterior, que continua no prontuário.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private Consulta retificacaoDe;

    @Column(length = 1000, updatable = false)
    private String motivoRetificacao;

    /** Hora do servidor em que o registro foi gravado (ADR-0062); nula em registros anteriores. */
    @Column(updatable = false)
    private Instant registradoEm;

    /** CPF do usuário autenticado quando o toggle de segurança está ligado (ADR-0055). */
    @Column(updatable = false)
    private String registradoPorCpf;

    public Consulta(Atendimento atendimento, Profissional profissional, LocalDateTime dataHora,
                     TipoConsulta tipoConsulta, String queixaPrincipal, String diagnostico,
                     String receituario, String examesSolicitados, LocalDate retorno) {
        this.atendimento = atendimento;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.tipoConsulta = tipoConsulta;
        this.queixaPrincipal = queixaPrincipal;
        this.diagnostico = diagnostico;
        this.receituario = receituario;
        this.examesSolicitados = examesSolicitados;
        this.retorno = retorno;
    }
}
