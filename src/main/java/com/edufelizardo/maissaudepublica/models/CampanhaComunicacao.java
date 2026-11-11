package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoAlvoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Campanha de comunicação (ADR-0174): objetivo, público, territórios, conteúdos e comunicados. Distinta da Campanha operacional de Programas. */
@Entity
@Table(name = "TB_CAMPANHA_COMUNICACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaComunicacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 500)
    private String objetivo;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoCampanhaComunicacao situacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PublicoAlvoCampanha publicoAlvo;

    @Column(length = 60)
    private String faixaEtaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_CAMPANHA_COMUNICACAO_TERRITORIO", joinColumns = @JoinColumn(name = "campanha_id"), inverseJoinColumns = @JoinColumn(name = "territorio_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Territorio> territorios = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TB_CAMPANHA_COMUNICACAO_TEMA", joinColumns = @JoinColumn(name = "campanha_id"))
    @Column(name = "tema", nullable = false, length = 100)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<String> temas = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_CAMPANHA_COMUNICACAO_CANAL", joinColumns = @JoinColumn(name = "campanha_id"), inverseJoinColumns = @JoinColumn(name = "canal_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CanalComunicacao> canais = new HashSet<>();

    /** Conteúdos pelo grupo de versões: mostra sempre a versão em vigor. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TB_CAMPANHA_COMUNICACAO_CONTEUDO", joinColumns = @JoinColumn(name = "campanha_id"))
    @Column(name = "conteudo_grupo_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UUID> conteudos = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Programa programa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_operacional_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Campanha campanhaOperacional;

    private LocalDateTime iniciadaEm;

    private LocalDateTime encerradaEm;

    @Column(length = 2000)
    private String balanco;

    @Column(length = 500)
    private String motivoCancelamento;

    /** C6 (ADR-0196): quantas pessoas alcançar nas ações educativas; opcional. */
    private Integer metaPessoas;

    /** C6 (ADR-0196): quantas ações educativas realizar; opcional. */
    private Integer metaAcoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradaEm;

    @Column(updatable = false)
    private String registradaPorCpf;
}
