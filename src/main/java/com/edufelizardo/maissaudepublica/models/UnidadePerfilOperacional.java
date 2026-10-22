package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Um perfil operacional na unidade, com início e fim (ADR-0117): histórico, não campo que se sobrescreve. Sem fim, está
 * vigente.
 */
@Entity
@Table(name = "TB_UNIDADE_PERFIL_OPERACIONAL", indexes = {
        @Index(name = "ix_unidade_perfil_operacional", columnList = "unidade_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadePerfilOperacional implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_operacional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PerfilOperacional perfil;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
