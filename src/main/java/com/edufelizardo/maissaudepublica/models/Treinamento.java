package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TreinamentoRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Catálogo de treinamentos (ver docs/rh/MODELO-RH.md, seção 8). {@code validadeMeses} nulo
 * significa treinamento sem validade (não expira) — usado por {@link ParticipacaoTreinamento}
 * pra calcular a data de validade da participação.
 */
@Entity
@Table(name = "TB_TREINAMENTO", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Treinamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String nome;

    private Integer cargaHoraria;

    private Integer validadeMeses;

    private boolean obrigatorio;

    /** Competências que o treinamento desenvolve (ADR-0173). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_TREINAMENTO_COMPETENCIA", joinColumns = @JoinColumn(name = "treinamento_id"), inverseJoinColumns = @JoinColumn(name = "competencia_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Competencia> competencias = new HashSet<>();

    /** Conteúdos educativos do treinamento, pelo grupo de versões: mostra sempre a versão em vigor (ADR-0173). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TB_TREINAMENTO_CONTEUDO", joinColumns = @JoinColumn(name = "treinamento_id"))
    @Column(name = "conteudo_grupo_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UUID> conteudos = new HashSet<>();

    public Treinamento(TreinamentoRequestDto dto) {
        this.nome = dto.getNome();
        this.cargaHoraria = dto.getCargaHoraria();
        this.validadeMeses = dto.getValidadeMeses();
        this.obrigatorio = dto.getObrigatorio();
    }
}
