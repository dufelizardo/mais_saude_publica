package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAph;
import com.edufelizardo.maissaudepublica.models.enuns.MecanismoTrauma;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaAph;
import com.edufelizardo.maissaudepublica.models.enuns.ProcedimentoAph;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Ficha clínica do atendimento pré-hospitalar (ADR-0188): o que a equipe da viatura encontrou e fez na cena da ocorrência de urgência
 * (ADR-0176), uma por ocorrência com recurso enviado. Finalizada, não muda; correção é adendo.
 */
@Entity
@Table(name = "TB_FICHA_APH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FichaAph implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id", referencedColumnName = "uuid", nullable = false, updatable = false, unique = true)
    private OcorrenciaUrgencia ocorrencia;

    @Enumerated(EnumType.STRING)
    private NaturezaAph natureza;

    @Enumerated(EnumType.STRING)
    private MecanismoTrauma mecanismoTrauma;

    @Column(length = 2000)
    private String situacaoEncontrada;

    private Boolean hemorragiaExsanguinante;

    @Column(length = 500)
    private String viaAerea;

    @Column(length = 500)
    private String respiracao;

    @Column(length = 500)
    private String circulacao;

    private Integer glasgowOcular;

    private Integer glasgowVerbal;

    private Integer glasgowMotor;

    @Column(length = 200)
    private String pupilas;

    @Column(length = 500)
    private String exposicao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_FICHA_APH_PROCEDIMENTO", joinColumns = @JoinColumn(name = "ficha_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "procedimento", nullable = false)
    private Set<ProcedimentoAph> procedimentos = new LinkedHashSet<>();

    @Column(length = 500)
    private String procedimentoOutro;

    @Column(length = 1000)
    private String impressaoDiagnostica;

    @Column(length = 10)
    private String cid;

    @Enumerated(EnumType.STRING)
    private DesfechoAph desfecho;

    /** Na recusa de remoção: quem recusou (o paciente ou o responsável). */
    @Column(length = 200)
    private String recusaNome;

    /** No óbito no local: a hora e o médico que constatou. */
    private Instant obitoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obito_constatado_por_id", referencedColumnName = "uuid")
    private Profissional obitoConstatadoPor;

    /** Entrega no destino: quem recebeu e a hora. */
    @Column(length = 200)
    private String recebidoPor;

    private Instant entregueEm;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant abertaEm;

    @Column(updatable = false)
    private String abertaPorCpf;

    private Instant atualizadaEm;

    private Instant finalizadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalizada_por_id", referencedColumnName = "uuid")
    private Profissional finalizadaPor;

    public boolean finalizada() {
        return finalizadaEm != null;
    }

    /** O total da escala de coma de Glasgow, quando os três componentes estão preenchidos. */
    public Integer glasgowTotal() {
        return glasgowOcular != null && glasgowVerbal != null && glasgowMotor != null ? glasgowOcular + glasgowVerbal + glasgowMotor : null;
    }
}
