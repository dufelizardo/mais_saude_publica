package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FonteEventoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEventoEmergencial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * O fenômeno (ADR-0156): enchente, surto, acidente coletivo. Nem todo evento vira emergência: é registrado, monitorado,
 * descartado ou convertido. Pode vir de um surto da Vigilância (ADR-0131).
 */
@Entity
@Table(name = "TB_EVENTO_EMERGENCIAL", indexes = @Index(name = "ix_evento_emergencial_situacao", columnList = "situacao, ocorrido_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoEmergencial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false)
    private TipoEmergencia tipo;

    @Column(nullable = false)
    private Instant ocorridoEm;

    @Column(length = 300)
    private String local;

    /** A área do evento em GeoJSON (WGS84), opcional. */
    @Column(columnDefinition = "text")
    private String geometria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FonteEventoEmergencial fonte;

    @Column(nullable = false, length = 2000)
    private String descricao;

    /** O surto que originou o evento, se houver. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surto_id", referencedColumnName = "uuid")
    private Surto surto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoEventoEmergencial situacao;

    @Column(length = 500)
    private String motivoDescarte;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
