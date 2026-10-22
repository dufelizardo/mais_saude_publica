package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoImovel;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Imóvel do cadastro territorial (ADR-0127): o lote ou o prédio, numa microárea, com endereço e, quando marcado no mapa,
 * o ponto (dentro da microárea, se ela tem desenho). O imóvel de moradia tem um ou mais {@link Domicilio}.
 */
@Entity
@Table(name = "TB_IMOVEL", indexes = @Index(name = "ix_imovel_microarea", columnList = "microarea_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Imovel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid", nullable = false)
    private Microarea microarea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoImovel tipo;

    @Embedded
    private Endereco endereco;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private boolean ativo;

    @Column(updatable = false)
    private String registradoPorCpf;
}
