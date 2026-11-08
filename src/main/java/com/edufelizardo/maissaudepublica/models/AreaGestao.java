package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TemaGestao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAreaGestao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Área da estrutura de gestão da secretaria (ADR-0175): gerência, coordenação ou supervisão técnica. A coordenação regional aponta para a regional. */
@Entity
@Table(name = "TB_AREA_GESTAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AreaGestao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAreaGestao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TemaGestao tema;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String sigla;

    @Column(length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "superior_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AreaGestao superior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regional_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude regional;

    @Column(nullable = false)
    private boolean ativa;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradaEm;
}
