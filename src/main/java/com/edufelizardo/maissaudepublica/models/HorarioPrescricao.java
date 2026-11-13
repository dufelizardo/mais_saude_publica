package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoHorarioPrescricao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Horário aprazado de um item da prescrição hospitalar (ADR-0187); a enfermagem pode reaprazar o pendente, com motivo. */
@Entity
@Table(name = "TB_HORARIO_PRESCRICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class HorarioPrescricao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ItemPrescricao item;

    @NotNull
    @Column(nullable = false)
    private Instant previstoPara;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoHorarioPrescricao situacao;

    /** O horário original, quando reaprazado. */
    private Instant aprazadoOriginalmente;

    @Column(length = 500)
    private String motivoReaprazamento;
}
