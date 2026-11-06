package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoLeishmanioseCanina;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoExameAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExameAnimal;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Exame do animal (ADR-0163): a amostra de raiva do agressor suspeito, o inquérito de leishmaniose visceral canina. */
@Entity
@Table(name = "TB_EXAME_ANIMAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameAnimal implements Serializable {
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
    @JoinColumn(name = "observacao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ObservacaoAnimal observacaoAnimal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoExameAnimal tipo;

    @Column(nullable = false, updatable = false)
    private LocalDate dataColeta;

    @Enumerated(EnumType.STRING)
    private ResultadoExameAnimal resultado;

    private Instant resultadoEm;

    @Enumerated(EnumType.STRING)
    private DesfechoLeishmanioseCanina desfechoLeishmaniose;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
