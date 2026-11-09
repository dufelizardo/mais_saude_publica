package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoUrgencia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Um passo da {@link OcorrenciaUrgencia} (ADR-0176), gravado uma vez: o tipo, a situação e a gravidade resultantes,
 * o texto, quem fez e quando. A hora pode ser informada (o rádio-operador registra a chegada depois de saber dela).
 */
@Entity
@Table(name = "TB_EVENTO_URGENCIA", indexes = @Index(name = "ix_evento_urgencia", columnList = "ocorrencia_id, ocorrido_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoUrgencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OcorrenciaUrgencia ocorrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private TipoEventoUrgencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private SituacaoOcorrenciaUrgencia situacaoResultante;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, updatable = false)
    private PrioridadeRegulacao prioridade;

    @Column(length = 2000, updatable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    public EventoUrgencia(OcorrenciaUrgencia o, TipoEventoUrgencia tipo, String texto, Profissional profissional, Instant ocorridoEm, String cpf) {
        this.ocorrencia = o;
        this.tipo = tipo;
        this.situacaoResultante = o.getSituacao();
        this.prioridade = o.getPrioridade();
        this.texto = texto;
        this.profissional = profissional;
        this.ocorridoEm = ocorridoEm;
        this.registradoPorCpf = cpf;
    }
}
