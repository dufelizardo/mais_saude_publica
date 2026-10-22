package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Tipo de intervenção em saúde mental (ADR-0121): catálogo como dado, semeado na V15. Intervenção em crise é da SM3.
 */
@Entity
@Table(name = "TB_TIPO_INTERVENCAO_SAUDE_MENTAL", uniqueConstraints = @UniqueConstraint(name = "uk_tipo_intervencao_sm_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoIntervencaoSaudeMental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 120, updatable = false)
    private String nome;

    @Column(nullable = false, updatable = false)
    private int ordem;

    @Column(nullable = false)
    private boolean ativo;
}
