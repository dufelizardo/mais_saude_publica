package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MomentoComunicacaoMp;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Comunicação da internação involuntária ao Ministério Público (Lei 10.216/2001, art. 8º; ADR-0123): em até 72 horas
 * da admissão e de novo na alta. Só inclusão.
 */
@Entity
@Table(name = "TB_COMUNICACAO_MINISTERIO_PUBLICO", uniqueConstraints = @UniqueConstraint(name = "uk_comunicacao_mp_momento", columnNames = {"internacao_id", "momento"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicacaoMinisterioPublico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Internacao internacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MomentoComunicacaoMp momento;

    @Column(nullable = false, updatable = false)
    private Instant comunicadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", updatable = false)
    private Profissional responsavel;

    @Column(length = 60, updatable = false)
    private String protocolo;

    @Column(length = 500, updatable = false)
    private String observacao;

    @Column(updatable = false)
    private String registradoPorCpf;
}
