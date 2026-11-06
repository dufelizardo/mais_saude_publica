package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Nível de resposta (ADR-0156): catálogo ordenado, de 0 a 3 na carga, com nome e descrição editáveis. */
@Entity
@Table(name = "TB_NIVEL_EMERGENCIA", uniqueConstraints = @UniqueConstraint(name = "uk_nivel_emergencia_ordem", columnNames = "ordem"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NivelEmergencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo;
}
