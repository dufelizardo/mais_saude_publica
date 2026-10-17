package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Capacidade assistencial (ADR-0117): o que uma unidade consegue fazer — classificação de risco, UTI, CAPS AD,
 * endodontia... Catálogo em tabela. O perfil é o grupo da capacidade: uma capacidade de grupo só vale na unidade que tem o
 * perfil vigente; sem perfil, é transversal (acolhimento, demanda espontânea) e vale em qualquer unidade de atendimento.
 */
@Entity
@Table(name = "TB_CAPACIDADE_ASSISTENCIAL", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CapacidadeAssistencial implements Serializable {
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

    /** Grupo da capacidade; nulo = transversal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_operacional_id", referencedColumnName = "uuid")
    private PerfilOperacional perfil;
}
