package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.NivelParticipacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * O detalhe educativo da ação coletiva de Programas (C3, ADR-0195): a ação educativa não ganha motor próprio (ADR-0114); este domínio
 * fornece o tema, os conteúdos revisados, os materiais distribuídos, as microáreas alvo, a campanha de comunicação e a avaliação.
 */
@Entity
@Table(name = "TB_ACAO_EDUCATIVA", uniqueConstraints = @UniqueConstraint(name = "uk_acao_educativa_acao", columnNames = "acao_id"),
        indexes = @Index(name = "ix_acao_educativa_campanha", columnList = "campanha_comunicacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoEducativa implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AcaoProgramatica acao;

    @Column(nullable = false, length = 120)
    private String tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_comunicacao_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CampanhaComunicacao campanhaComunicacao;

    /** Os conteúdos educativos pelo grupo de versões: mostra-se a versão revisada em vigor. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_ACAO_EDUCATIVA_CONTEUDO", joinColumns = @JoinColumn(name = "acao_educativa_id"))
    @Column(name = "conteudo_grupo_id", nullable = false)
    private Set<UUID> conteudos = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_ACAO_EDUCATIVA_MICROAREA", joinColumns = @JoinColumn(name = "acao_educativa_id"), inverseJoinColumns = @JoinColumn(name = "microarea_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Microarea> microareas = new HashSet<>();

    @OneToMany(mappedBy = "acaoEducativa", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AcaoEducativaMaterial> materiais = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private NivelParticipacao participacao;

    @Column(length = 1000)
    private String avaliacao;

    private Instant avaliadoEm;

    private String avaliadoPorCpf;

    @Column(nullable = false)
    private Instant atualizadoEm;

    private String atualizadoPorCpf;
}
