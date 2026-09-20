package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusVaga;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Vaga de recrutamento, ligada a uma {@link UnidadeDeSaude} e a um {@link Cargo} (ver
 * docs/rh/MODELO-RH.md, seção 10). Reaproveita {@code Cargo} (Fase 0) em vez de um campo de
 * texto livre.
 */
@Entity
@Table(name = "TB_VAGA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Vaga implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", referencedColumnName = "uuid", nullable = false)
    private Cargo cargo;

    @NotNull
    private Integer quantidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusVaga status;

    public Vaga(UnidadeDeSaude unidade, Cargo cargo, Integer quantidade, StatusVaga status) {
        this.unidade = unidade;
        this.cargo = cargo;
        this.quantidade = quantidade;
        this.status = status;
    }
}
