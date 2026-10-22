package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Perfil operacional de uma unidade de saúde (ADR-0117, desenho na ADR-0115): o que a unidade opera — atenção primária,
 * urgência, internação, atenção psicossocial... Catálogo em tabela, não enum, e associado à unidade, não ao tipo: o tipo
 * estrutural não determina sozinho o comportamento.
 */
@Entity
@Table(name = "TB_PERFIL_OPERACIONAL", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerfilOperacional implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false, length = 60)
    private String codigo;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private boolean ativo;
}
