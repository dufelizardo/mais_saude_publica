package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Contato do plano (ADR-0157): Defesa Civil, coordenação, serviços. */
@Entity
@Table(name = "TB_CONTATO_PLANO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ContatoPlano implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PlanoContingencia plano;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 200)
    private String papel;

    @Column(length = 30)
    private String telefone;
}
