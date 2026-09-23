package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Subdivisão organizacional de uma {@link UnidadeDeSaude} (ver
 * docs/adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md). Genérico por
 * {@link TipoSetor} — "Setor Administrativo" é apenas um Setor com tipo {@code ADMINISTRATIVO},
 * não uma subclasse própria.
 */
@Entity
@Table(name = "TB_SETOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Setor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @NotBlank
    private String nome;

    @NotBlank
    private String codigo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoSetor tipo;

    private boolean ativo;

    public Setor(UnidadeDeSaude unidade, String nome, String codigo, TipoSetor tipo, boolean ativo) {
        this.unidade = unidade;
        this.nome = nome;
        this.codigo = codigo;
        this.tipo = tipo;
        this.ativo = ativo;
    }
}
