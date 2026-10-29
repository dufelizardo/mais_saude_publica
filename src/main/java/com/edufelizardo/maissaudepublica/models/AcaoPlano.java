package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Ação prevista no plano para um nível (ADR-0157): o responsável (função ou setor), o prazo a partir da ativação e a unidade. */
@Entity
@Table(name = "TB_ACAO_PLANO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoPlano implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PlanoContingencia plano;

    @Column(nullable = false)
    private int ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_id", referencedColumnName = "uuid", nullable = false)
    private NivelEmergencia nivel;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, length = 200)
    private String responsavel;

    /** Horas a partir de quando a emergência chegou ao nível com o plano ativo. */
    private Integer prazoHoras;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid")
    private UnidadeDeSaude unidade;
}
