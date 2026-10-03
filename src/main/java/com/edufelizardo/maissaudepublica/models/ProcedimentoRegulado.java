package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Catálogo do que passa pela Central de Regulação do Acesso (ADR-0087): consultas especializadas, exames
 * e procedimentos. Não se apaga: sai de uso com {@code ativo = false}, e as solicitações antigas continuam
 * apontando para ele.
 */
@Entity
@Table(name = "TB_PROCEDIMENTO_REGULADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcedimentoRegulado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false, unique = true, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProcedimentoRegulado tipo;

    private boolean ativo;

    public ProcedimentoRegulado(String nome, TipoProcedimentoRegulado tipo, boolean ativo) {
        this.nome = nome;
        this.tipo = tipo;
        this.ativo = ativo;
    }
}
