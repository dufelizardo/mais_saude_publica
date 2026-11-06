package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoObservacaoAnimal;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Observação do animal agressor por 10 dias (ADR-0163), ligada ao atendimento antirrábico humano notificado. */
@Entity
@Table(name = "TB_OBSERVACAO_ANIMAL", indexes = @Index(name = "ix_observacao_animal_notificacao", columnList = "notificacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ObservacaoAnimal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AnimalZoonoses animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Notificacao notificacao;

    @Column(nullable = false, updatable = false)
    private LocalDate dataAgressao;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    @Column(nullable = false, updatable = false)
    private LocalDate fimPrevisto;

    @Enumerated(EnumType.STRING)
    private DesfechoObservacaoAnimal desfecho;

    private LocalDate desfechoEm;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
