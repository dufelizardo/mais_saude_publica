package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DecisaoRegulacaoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.LocalRealizacao;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRecursoUrgencia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Ocorrência de urgência (ADR-0176): o chamado que chega à central de regulação das urgências (192), o pedido de
 * transferência de uma unidade ou o acionamento pela crise em saúde mental. O atendente abre; o médico regulador
 * classifica, decide e indica o destino; os deslocamentos e o recebimento no destino ficam em {@link EventoUrgencia}.
 * O paciente é opcional: na rua, muitas vezes ninguém sabe quem é, e ele é vinculado depois.
 */
@Entity
@Table(name = "TB_OCORRENCIA_URGENCIA", indexes = {
        @Index(name = "ix_ocorrencia_urgencia_central", columnList = "central_id, situacao"),
        @Index(name = "ix_ocorrencia_urgencia_paciente", columnList = "paciente_id"),
        @Index(name = "ix_ocorrencia_urgencia_destino", columnList = "destino_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcorrenciaUrgencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** 2026-000123: sequencial por ano, para informar ao telefone. */
    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String protocolo;

    /** A unidade com a capacidade de regulação médica de urgência (modelo operacional, MO4). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "central_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude central;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private OrigemOcorrenciaUrgencia origem;

    @Column(length = 100, updatable = false)
    private String solicitanteNome;

    @Column(length = 20, updatable = false)
    private String solicitanteTelefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_solicitante_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidadeSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crise_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EventoCrise crise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    /** Sem identificação: sexo, idade aproximada, roupa. */
    @Column(length = 200)
    private String descricaoPaciente;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, updatable = false)
    private LocalRealizacao localTipo;

    @Column(length = 300, updatable = false)
    private String endereco;

    @Column(length = 300, updatable = false)
    private String referencia;

    @Column(updatable = false)
    private Double latitude;

    @Column(updatable = false)
    private Double longitude;

    /** Dado de saúde. */
    @Column(nullable = false, length = 2000, updatable = false)
    private String queixa;

    @Column(nullable = false, updatable = false)
    private Instant recebidaEm;

    @Column(updatable = false)
    private String recebidaPorCpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional atendente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoOcorrenciaUrgencia situacao;

    /** Na regulação; antes dela, nula (aguardando o médico regulador). */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PrioridadeRegulacao prioridade;

    private Instant reguladaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regulador_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional regulador;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DecisaoRegulacaoUrgencia decisao;

    /** A conduta do regulador. Dado de saúde. */
    @Column(length = 2000)
    private String conduta;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoRecursoUrgencia tipoRecurso;

    /** A viatura em texto até o Transporte (#16). */
    @Column(length = 60)
    private String recursoIdentificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude destino;

    /** Encaminhado sem vaga confirmada, prerrogativa do regulador; a justificativa é obrigatória. */
    @Column(nullable = false)
    private boolean vagaZero;

    @Column(length = 500)
    private String justificativaVagaZero;

    private Instant recebidaNoDestinoEm;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DesfechoOcorrenciaUrgencia desfecho;

    private Instant encerradaEm;

    public boolean aberta() {
        return situacao == SituacaoOcorrenciaUrgencia.RECEBIDA || situacao == SituacaoOcorrenciaUrgencia.REGULADA
                || situacao == SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO;
    }

    /** Há recurso da central em deslocamento: envio de recurso, ou transferência com recurso. */
    public boolean comRecurso() {
        return tipoRecurso != null;
    }
}
