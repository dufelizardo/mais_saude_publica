package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDecretoEmergencia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A situação que exige resposta coordenada e extraordinária (ADR-0156, desenho na ADR-0113): a entidade central do
 * domínio. Nasce de um evento; o nível muda com histórico ({@link MudancaNivelEmergencia}); o território afetado é o
 * polígono e as microáreas do Território. A ESPIN e o decreto são registrados: o sistema não declara.
 */
@Entity
@Table(name = "TB_EMERGENCIA", uniqueConstraints = @UniqueConstraint(name = "uk_emergencia_evento", columnNames = "evento_id"),
        indexes = @Index(name = "ix_emergencia_situacao", columnList = "situacao, inicio"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Emergencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private EventoEmergencial evento;

    @Column(nullable = false, length = 200)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false)
    private TipoEmergencia tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_id", referencedColumnName = "uuid", nullable = false)
    private NivelEmergencia nivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoEmergencia situacao;

    @Column(nullable = false, updatable = false)
    private Instant inicio;

    private Instant fim;

    @Column(length = 1000)
    private String motivoEncerramento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid")
    private Profissional responsavel;

    /** O polígono do território afetado (GeoJSON, WGS84); pode não coincidir com as microáreas. */
    @Column(columnDefinition = "text")
    private String geometria;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_EMERGENCIA_MICROAREA", joinColumns = @JoinColumn(name = "emergencia_id"), inverseJoinColumns = @JoinColumn(name = "microarea_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Microarea> microareas = new HashSet<>();

    /** Emergência em Saúde Pública de Importância Nacional (Decreto 7.616/2011). */
    @Column(length = 60)
    private String espinNumero;

    private LocalDate espinData;

    @Enumerated(EnumType.STRING)
    private TipoDecretoEmergencia decretoTipo;

    @Column(length = 60)
    private String decretoNumero;

    private LocalDate decretoData;

    @Column(updatable = false)
    private String registradoPorCpf;
}
