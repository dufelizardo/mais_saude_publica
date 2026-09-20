package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Cargo dentro de uma {@link CategoriaSalarial} — é daqui que o salário efetivo de um profissional
 * é derivado (via {@code Lotacao.cargo}, ver docs/rh/MODELO-RH.md seção 2.2/2.4). Não tem
 * unicidade de nome — o mesmo nome de cargo pode existir em categorias diferentes.
 */
@Entity
@Table(name = "TB_CARGO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Cargo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_salarial_id", referencedColumnName = "uuid", nullable = false)
    private CategoriaSalarial categoria;

    @NotBlank
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    public Cargo(CategoriaSalarial categoria, String nome) {
        this.categoria = categoria;
        this.nome = nome;
    }

    public Cargo(CategoriaSalarial categoria, String nome, String descricao) {
        this.categoria = categoria;
        this.nome = nome;
        this.descricao = descricao;
    }
}
