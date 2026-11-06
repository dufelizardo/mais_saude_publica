package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroEtapa;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Registro guardado da etapa do percurso (ADR-0152): a etapa manual cumprida (o teste rápido, o teste do pezinho) ou a
 * etapa dispensada, com o motivo. As outras se cumprem pelos registros dos domínios.
 */
@Entity
@Table(name = "TB_REGISTRO_ETAPA_PERCURSO", indexes = @Index(name = "ix_registro_etapa_percurso_percurso", columnList = "percurso_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroEtapaPercurso implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "percurso_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PercursoLinhaCuidado percurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private EtapaLinhaCuidado etapa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoRegistroEtapa tipo;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(length = 500, updatable = false)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
