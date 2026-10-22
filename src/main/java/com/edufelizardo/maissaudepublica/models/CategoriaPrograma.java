package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Categoria cadastrável dos programas de saúde (ADR-0118): Atenção Básica, Crônicos, Ciclos de vida… */
@Entity
@Table(name = "TB_CATEGORIA_PROGRAMA", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CategoriaPrograma implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String nome;

    /** Ordem das abas e dos grupos na tela. */
    @Column(nullable = false)
    private int ordem;

    private boolean ativa;
}
