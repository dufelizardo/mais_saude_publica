package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Leito de internação ou de observação (ADR-0098), num setor assistencial da unidade. Não se apaga: sai de uso. */
@Entity
@Table(name = "TB_LEITO", uniqueConstraints = @UniqueConstraint(name = "uk_leito_identificacao_por_unidade",
        columnNames = {"unidade_id", "identificacao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Leito implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", nullable = false)
    private Setor setor;

    /** Como a equipe chama o leito, ex.: "Enf. 2 · Leito 03". Única na unidade. */
    @Column(nullable = false, length = 60)
    private String identificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLeito tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexoLeito sexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoLeito situacao;

    @Column(length = 500)
    private String motivoBloqueio;

    @Column(nullable = false)
    private boolean ativo;
}
