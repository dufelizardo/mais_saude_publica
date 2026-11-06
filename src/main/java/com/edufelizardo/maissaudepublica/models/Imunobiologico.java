package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.ViaVacina;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Imunobiológico (ADR-0147): vacina, soro ou imunoglobulina, com a via, o volume da dose, as doses por frasco e a
 * validade depois de aberto (multidose). Catálogo próprio, separado do medicamento da Farmácia; carga inicial com o
 * Calendário Nacional de Vacinação, editável.
 */
@Entity
@Table(name = "TB_IMUNOBIOLOGICO", uniqueConstraints = @UniqueConstraint(name = "uk_imunobiologico_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Imunobiologico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 30)
    private String sigla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoImunobiologico tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViaVacina via;

    @Column(precision = 5, scale = 2)
    private BigDecimal volumeDoseMl;

    @Column(nullable = false)
    private int dosesPorFrasco;

    /** Multidose: por quantas horas o frasco aberto vale. */
    private Integer validadeAposAbertoHoras;

    /** Código do imunobiológico no SI-PNI (integração na I5). */
    @Column(length = 20)
    private String codigoSipni;

    @Column(nullable = false)
    private boolean ativo;
}
