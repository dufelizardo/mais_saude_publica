package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Catálogo de funcionalidades/competências administrativas (ver
 * docs/adr/0032-catalogo-de-capacidades-administrativas.md) — dado em tabela, não enum Java, pra
 * que uma capacidade nova (ex.: novo tipo de unidade de saúde) não exija alteração de código.
 * Não é controle de acesso: decide se algo existe para uma unidade, não quem pode operá-lo.
 */
@Entity
@Table(name = "TB_CAPACIDADE_ADMINISTRATIVA", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeAdministrativa implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String codigo;

    @NotBlank
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private boolean ativo;

    public CapacidadeAdministrativa(String codigo, String nome, String descricao, boolean ativo) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
    }
}
