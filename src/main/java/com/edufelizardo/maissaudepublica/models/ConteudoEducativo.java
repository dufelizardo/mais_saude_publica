package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Conteúdo educativo (ADR-0168): o conhecimento, com revisão técnica, validade e versão. As versões do mesmo conteúdo dividem o grupo. */
@Entity
@Table(name = "TB_CONTEUDO_EDUCATIVO", indexes = @Index(name = "ix_conteudo_grupo", columnList = "grupo_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConteudoEducativo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(name = "grupo_id", nullable = false, updatable = false)
    private UUID grupoId;

    @Column(nullable = false, updatable = false)
    private int versao;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String resumo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false, length = 100)
    private String tema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicoComunicacao publico;

    @Column(length = 60)
    private String faixaEtaria;

    @Column(nullable = false)
    private LocalDate validade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoConteudo situacao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_CONTEUDO_CANAL", joinColumns = @JoinColumn(name = "conteudo_id"), inverseJoinColumns = @JoinColumn(name = "canal_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CanalComunicacao> canais = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private String autorCpf;

    private String revisorCpf;

    private LocalDateTime revisadoEm;

    @Column(length = 500)
    private String motivoDevolucao;

    private LocalDateTime publicadoEm;
}
