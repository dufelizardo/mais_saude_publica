package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Ponto de reposição do material na unidade (ADR-0177): abaixo dele, o material entra nos alertas. */
@Entity
@Table(name = "TB_ESTOQUE_MINIMO", uniqueConstraints = @UniqueConstraint(name = "uk_estoque_minimo", columnNames = {"material_id", "unidade_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstoqueMinimo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MaterialEstoque material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Column(nullable = false)
    private Integer quantidadeMinima;

    @Column(nullable = false)
    private Instant atualizadoEm;

    private String atualizadoPorCpf;
}
