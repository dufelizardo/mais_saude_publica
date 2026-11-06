package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TurnoLeituraTemperatura;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Leitura de temperatura da câmara (ADR-0148), duas por dia: a atual, a mínima e a máxima. Fora da faixa é excursão.
 */
@Entity
@Table(name = "TB_LEITURA_TEMPERATURA", indexes = @Index(name = "ix_leitura_temperatura_camara", columnList = "camara_id, lida_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LeituraTemperatura implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camara_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private CamaraRefrigeracao camara;

    @Column(nullable = false, updatable = false)
    private LocalDateTime lidaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TurnoLeituraTemperatura turno;

    @Column(nullable = false, precision = 4, scale = 1, updatable = false)
    private BigDecimal temperaturaAtual;

    @Column(nullable = false, precision = 4, scale = 1, updatable = false)
    private BigDecimal temperaturaMinima;

    @Column(nullable = false, precision = 4, scale = 1, updatable = false)
    private BigDecimal temperaturaMaxima;

    @Column(nullable = false, updatable = false)
    private boolean excursao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @Column(length = 500, updatable = false)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
