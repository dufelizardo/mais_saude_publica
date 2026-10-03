package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLeito;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Movimento num leito, com a internação quando houver (ADR-0098). Só inclusão. */
@Entity
@Table(name = "TB_EVENTO_LEITO", indexes = {
        @Index(name = "ix_evento_leito_leito", columnList = "leito_id, ocorrido_em"),
        @Index(name = "ix_evento_leito_internacao", columnList = "internacao_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoLeito implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leito_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Leito leito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid", updatable = false)
    private Internacao internacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEventoLeito tipo;

    @Column(length = 1000, updatable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
