package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Canal de comunicação (ADR-0168): catálogo; o domínio não conhece o fornecedor, só o tipo. */
@Entity
@Table(name = "TB_CANAL_COMUNICACAO", uniqueConstraints = @UniqueConstraint(name = "uk_canal_comunicacao_nome", columnNames = "nome"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CanalComunicacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCanalComunicacao tipo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo;
}
