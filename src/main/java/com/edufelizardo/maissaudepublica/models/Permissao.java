package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Permissão do catálogo, no formato {@code RECURSO.ACAO} (ADR-0054, ADR-0066). Catálogo como dado:
 * uma permissão nova é uma linha, não um enum novo. Semeada na subida por {@code CatalogoDeAcesso}.
 */
@Entity
@Table(name = "TB_PERMISSAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Permissao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DimensaoPermissao dimensao;

    public Permissao(String codigo, String descricao, DimensaoPermissao dimensao) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.dimensao = dimensao;
    }
}
