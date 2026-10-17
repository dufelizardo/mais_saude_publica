package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Programa de saúde (ADR-0118, desenho na ADR-0109): organização permanente de ações para um público, uma condição ou um
 * objetivo — HiperDia, Pré-natal, Saúde do Idoso. Cadastrável, sem enum. O público-alvo é dado do programa e só descreve
 * quem ele atende: não inscreve ninguém sozinho.
 */
@Entity
@Table(name = "TB_PROGRAMA", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Programa implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String codigo;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", referencedColumnName = "uuid", nullable = false)
    private CategoriaPrograma categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbrangenciaPrograma abrangencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoPrograma situacao;

    private LocalDate inicio;

    private LocalDate fim;

    /** Coordenação assistencial (profissional do RH). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordenador_id", referencedColumnName = "uuid")
    private Profissional coordenador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodicidadeAcompanhamento periodicidade;

    /** Estar no programa revela diagnóstico (saúde mental, IST): a P2 restringe ainda mais quem vê o acompanhamento. */
    private boolean sensivel;

    // ── Público-alvo ───────────────────────────────────────────────────────────────────────────────

    private Integer idadeMinima;

    private Integer idadeMaxima;

    /** Nulo: qualquer sexo. */
    @Enumerated(EnumType.STRING)
    private Sexo publicoSexo;

    private boolean gestante;

    /** Códigos CID-10 ou CIAP-2 das condições acompanhadas (ex.: I10, E11, T90). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_PROGRAMA_CONDICAO", joinColumns = @JoinColumn(name = "programa_id"))
    @Column(name = "codigo", length = 10, nullable = false)
    @OrderColumn(name = "posicao")
    private List<String> condicoes = new ArrayList<>();
}
