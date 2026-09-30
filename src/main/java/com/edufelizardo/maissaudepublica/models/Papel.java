package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Papel de acesso: um conjunto de permissões (ADR-0054, ADR-0066). Diferente de {@code Cargo} (RH):
 * conceder papel é ato administrativo separado. Os papéis padrão são semeados na subida; outros podem
 * ser criados pela API.
 */
@Entity
@Table(name = "TB_PAPEL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "permissoes")
@EqualsAndHashCode(exclude = "permissoes")
public class Papel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** Identificador estável (ex.: ENFERMEIRO), em maiúsculas. */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank
    private String nome;

    @Column(length = 1000)
    private String descricao;

    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TB_PAPEL_PERMISSAO",
            joinColumns = @JoinColumn(name = "papel_id", referencedColumnName = "uuid"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id", referencedColumnName = "uuid"))
    private Set<Permissao> permissoes = new HashSet<>();

    public Papel(String codigo, String nome, String descricao, Set<Permissao> permissoes) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.permissoes = permissoes;
        this.ativo = true;
    }
}
