package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Microárea (ADR-0125): o espaço de atuação de um ACS ({@link AtribuicaoMicroarea}), dentro da área e sem se sobrepor às
 * outras. A população de referência acima de {@value #POPULACAO_REFERENCIA_CNES} pessoas (CNES) gera aviso, não bloqueio.
 */
@Entity
@Table(name = "TB_MICROAREA", uniqueConstraints = @UniqueConstraint(name = "uk_microarea_codigo_por_area", columnNames = {"area_id", "codigo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Microarea implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    public static final int POPULACAO_REFERENCIA_CNES = 750;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Area area;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(length = 120)
    private String nome;

    @Column(columnDefinition = "text")
    private String geometria;

    private Integer populacaoReferencia;
}
