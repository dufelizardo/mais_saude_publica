package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSurto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoSurto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSurto;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Surto (ADR-0131, desenho na ADR-0110): evento epidemiológico que relaciona casos do mesmo agravo, num território
 * (microáreas) ou num estabelecimento, com a definição de caso própria, a investigação e as medidas de controle. Não é
 * "vários casos": é aberto por quem investiga, e o mapa e a curva ajudam a ver.
 */
@Entity
@Table(name = "TB_SURTO", indexes = @Index(name = "ix_surto_unidade", columnList = "unidade_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Surto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agravo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Agravo agravo;

    /** A unidade responsável pela investigação (escopo de acesso). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSurto tipo;

    @Enumerated(EnumType.STRING)
    private TipoInstituicaoSurto tipoInstituicao;

    @Column(length = 200)
    private String nomeInstituicao;

    @Column(length = 500)
    private String enderecoInstituicao;

    /** As microáreas afetadas (comunitário). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_SURTO_MICROAREA", joinColumns = @JoinColumn(name = "surto_id"), inverseJoinColumns = @JoinColumn(name = "microarea_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Microarea> microareas = new HashSet<>();

    /** O critério do surto (quem entra), que não é o da ficha do agravo. */
    @Column(length = 2000)
    private String definicaoCaso;

    @Column(nullable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoSurto situacao;

    @Column(length = 2000)
    private String conclusao;

    private Instant encerradoEm;

    @Column(nullable = false, updatable = false)
    private Instant abertoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
