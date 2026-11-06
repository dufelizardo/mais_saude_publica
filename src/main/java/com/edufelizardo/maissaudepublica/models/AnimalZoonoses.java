package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Animal de interesse das zoonoses (ADR-0163): o agressor em observação, o cão do inquérito de leishmaniose. O tutor não vira paciente: só o nome e o telefone. */
@Entity
@Table(name = "TB_ANIMAL_ZOONOSES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnimalZoonoses implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EspecieAnimal especie;

    @Column(length = 200)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(length = 200)
    private String tutorNome;

    @Column(length = 40)
    private String tutorTelefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imovel_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Imovel imovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Microarea microarea;

    @Column(length = 300)
    private String endereco;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
