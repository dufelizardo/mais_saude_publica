package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.EstrategiaVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.LocalAplicacaoVacina;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemRegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaVacina;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de vacinação (ADR-0147): a dose aplicada na unidade, ou transcrita da caderneta (sem exigir lote). Registro
 * clínico imutável: a correção é uma nova versão ligada à anterior (ADR-0062), e o registrado por engano é anulado com
 * motivo. Vigente = nem anulado, nem retificado.
 */
@Entity
@Table(name = "TB_REGISTRO_VACINACAO", indexes = {
        @Index(name = "ix_registro_vacinacao_paciente", columnList = "paciente_id"),
        @Index(name = "ix_registro_vacinacao_unidade_data", columnList = "unidade_id, aplicada_em")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroVacinacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imunobiologico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Imunobiologico imunobiologico;

    /** A dose do calendário a que corresponde, quando há. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "esquema_id", referencedColumnName = "uuid", updatable = false)
    private EsquemaVacinal esquema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DoseVacinal dose;

    @Column(nullable = false, updatable = false)
    private LocalDateTime aplicadaEm;

    /** A unidade que registrou (onde aplicou, ou onde transcreveu a caderneta). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    /** Quem aplicou (obrigatório na aplicada). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrigemRegistroVacinacao origem;

    /** Na transcrita: onde a dose foi aplicada, como consta na caderneta. */
    @Column(length = 200, updatable = false)
    private String localOrigem;

    @Column(length = 40, updatable = false)
    private String lote;

    @Column(length = 100, updatable = false)
    private String fabricante;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private ViaVacina via;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private LocalAplicacaoVacina localAplicacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EstrategiaVacinacao estrategia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", referencedColumnName = "uuid", updatable = false)
    private Campanha campanha;

    @Column(nullable = false, updatable = false)
    private boolean gestante;

    /** Dose fora da idade ou do intervalo do calendário: o porquê. */
    @Column(length = 500, updatable = false)
    private String justificativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", updatable = false)
    private Atendimento atendimento;

    @Column(length = 500, updatable = false)
    private String observacao;

    /** Retificação (ADR-0062): a versão que esta corrige. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private RegistroVacinacao retificacaoDe;

    @Column(length = 500, updatable = false)
    private String motivoRetificacao;

    /** Registrado por engano (paciente errado, dose que não houve): sai da caderneta, com o motivo. */
    @Column(nullable = false)
    private boolean anulada;

    @Column(length = 500)
    private String motivoAnulacao;

    private Instant anuladaEm;

    private String anuladaPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
