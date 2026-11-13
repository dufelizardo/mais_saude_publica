package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLaudoAih;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Histórico do laudo de AIH (ADR-0186): solicitação, correção, devolução, autorização, negação e cancelamento. */
@Entity
@Table(name = "TB_EVENTO_LAUDO_AIH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoLaudoAih implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laudo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LaudoAih laudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEventoLaudoAih tipo;

    @Column(length = 1000, updatable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
