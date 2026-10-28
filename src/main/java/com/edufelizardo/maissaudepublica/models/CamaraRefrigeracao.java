package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Câmara de refrigeração da sala de vacina (ADR-0148), com a faixa de temperatura (padrão 2 a 8 °C).
 */
@Entity
@Table(name = "TB_CAMARA_REFRIGERACAO", uniqueConstraints = @UniqueConstraint(name = "uk_camara_unidade_nome", columnNames = {"unidade_id", "nome"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CamaraRefrigeracao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal temperaturaMinima;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal temperaturaMaxima;

    @Column(nullable = false)
    private boolean ativa;
}
