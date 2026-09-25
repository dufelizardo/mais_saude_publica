package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Detalhamento opcional de uma {@link CapacidadeAdministrativa} (ver
 * docs/adr/0033-processos-administrativos-por-capacidade.md) — uma capacidade pode existir e estar
 * habilitada sem nenhum processo cadastrado ainda ("declarada, não operacional"). Processos
 * concretos só devem ser cadastrados quando o recurso administrativo correspondente existir de
 * fato (mesmo princípio da "regra da fatia 8" do RH).
 */
@Entity
@Table(name = "TB_PROCESSO_ADMINISTRATIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcessoAdministrativo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacidade_administrativa_id", referencedColumnName = "uuid", nullable = false)
    private CapacidadeAdministrativa capacidade;

    @NotBlank
    private String codigo;

    @NotBlank
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private boolean ativo;

    public ProcessoAdministrativo(CapacidadeAdministrativa capacidade, String codigo, String nome,
                                   String descricao, boolean ativo) {
        this.capacidade = capacidade;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
    }
}
