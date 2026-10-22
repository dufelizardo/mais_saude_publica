package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.EtapaCriseSaudeMental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Uma etapa da crise (ADR-0123): avaliação, intervenção, acionamento da rede, encaminhamento ou pós-crise. Só inclusão.
 */
@Entity
@Table(name = "TB_ETAPA_CRISE", indexes = @Index(name = "ix_etapa_crise", columnList = "crise_id, ocorrida_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EtapaCrise implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crise_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private EventoCrise crise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EtapaCriseSaudeMental etapa;

    @Column(nullable = false, length = 2000, updatable = false)
    private String descricao;

    @Column(nullable = false, updatable = false)
    private Instant ocorridaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(updatable = false)
    private String registradoPorCpf;
}
