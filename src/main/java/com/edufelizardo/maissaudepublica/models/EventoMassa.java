package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NaturezaEventoMassa;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoEventoMassa;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Os dados do evento de massa (ADR-0160): natureza, local, período previsto, público, organizador e risco. Um por emergência. */
@Entity
@Table(name = "TB_EVENTO_MASSA", uniqueConstraints = @UniqueConstraint(name = "uk_evento_massa_emergencia", columnNames = "emergencia_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoMassa implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NaturezaEventoMassa natureza;

    @Column(nullable = false, length = 300)
    private String local;

    @Column(nullable = false)
    private Instant inicioPrevisto;

    @Column(nullable = false)
    private Instant fimPrevisto;

    @Column(nullable = false)
    private int publicoEstimado;

    private Integer publicoMaximo;

    @Column(length = 200)
    private String organizador;

    @Column(length = 200)
    private String contato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiscoEventoMassa risco;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false)
    private Instant atualizadoEm;

    private String registradoPorCpf;
}
