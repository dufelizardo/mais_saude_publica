package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampanha;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Campanha de saúde (ADR-0133, desenho na ADR-0109): iniciativa com início e fim, público, meta e locais — a vacinação
 * contra a gripe, o Outubro Rosa, o mutirão de catarata. Pode estar ligada a um programa, mas não é um programa.
 * Cadastrável pelo município; só a publicada aparece na consulta pública.
 */
@Entity
@Table(name = "TB_CAMPANHA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Campanha implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCampanha tipo;

    /** O programa a que a campanha serve (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", referencedColumnName = "uuid")
    private Programa programa;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    // ── Público-alvo (como no programa) ────────────────────────────────────────────────────────────

    private Integer idadeMinima;

    private Integer idadeMaxima;

    @Enumerated(EnumType.STRING)
    private Sexo publicoSexo;

    private boolean gestante;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_CAMPANHA_CONDICAO", joinColumns = @JoinColumn(name = "campanha_id"))
    @Column(name = "codigo", length = 10, nullable = false)
    @OrderColumn(name = "posicao")
    private List<String> condicoes = new ArrayList<>();

    /** O público em palavras ("crianças de 6 meses a 5 anos"), quando os campos não bastam. */
    @Column(length = 300)
    private String publicoDescricao;

    // ── Meta ───────────────────────────────────────────────────────────────────────────────────────

    @Column(length = 300)
    private String metaDescricao;

    /**
     * Quantas pessoas a campanha quer alcançar. O andamento soma o atendido nas ações realizadas; na de vacinação, conta
     * as pessoas vacinadas com a campanha registrada na dose (ADR-0149).
     */
    private Integer metaQuantidade;

    /** Vacinação: as vacinas da campanha (opcional); com elas, a dose de outra vacina não entra na campanha (ADR-0149). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TB_CAMPANHA_IMUNOBIOLOGICO", joinColumns = @JoinColumn(name = "campanha_id"),
            inverseJoinColumns = @JoinColumn(name = "imunobiologico_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Imunobiologico> imunobiologicos = new HashSet<>();

    /** Só a publicada aparece na consulta pública. */
    private boolean publicada;

    private Instant canceladaEm;

    @Column(length = 500)
    private String motivoCancelamento;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** Pelas datas, ou cancelada. */
    public SituacaoCampanha situacao(LocalDate hoje) {
        if (canceladaEm != null) {
            return SituacaoCampanha.CANCELADA;
        }
        return hoje.isBefore(inicio) ? SituacaoCampanha.PLANEJADA : hoje.isAfter(fim) ? SituacaoCampanha.ENCERRADA : SituacaoCampanha.EM_ANDAMENTO;
    }
}
