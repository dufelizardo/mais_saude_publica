package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoSegmentoTerritorial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Segmento territorial (ADR-0125): conjunto de áreas contíguas, como um distrito sanitário ou uma zona do IBGE. A
 * unidade de gestão, quando há, dá o escopo de quem cadastra; sem ela, o cadastro é da rede inteira.
 */
@Entity
@Table(name = "TB_SEGMENTO_TERRITORIAL", uniqueConstraints = @UniqueConstraint(name = "uk_segmento_territorial_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SegmentoTerritorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSegmentoTerritorial tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_gestao_id", referencedColumnName = "uuid")
    private UnidadeDeSaude unidadeGestao;

    /** Polygon ou MultiPolygon em GeoJSON (WGS84); opcional. */
    @Column(columnDefinition = "text")
    private String geometria;
}
