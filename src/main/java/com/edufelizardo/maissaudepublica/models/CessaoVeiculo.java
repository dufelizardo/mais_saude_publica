package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Veículo cedido pela frota a uma mobilização de veículo da emergência (T3, ADR-0192). Cedido, fica em serviço pelo mecanismo da T1;
 * devolvido, volta a disponível com a quilometragem. A lotação do veículo não muda.
 */
@Entity
@Table(name = "TB_CESSAO_VEICULO", indexes = {@Index(name = "ix_cessao_veiculo_mobilizacao", columnList = "mobilizacao_id"),
        @Index(name = "ix_cessao_veiculo_veiculo", columnList = "veiculo_id, devolvida_em")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CessaoVeiculo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mobilizacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Mobilizacao mobilizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Veiculo veiculo;

    @Column(nullable = false, updatable = false)
    private Instant cedidaEm;

    private Instant devolvidaEm;

    @Column(updatable = false)
    private Integer kmInicio;

    private Integer kmFim;

    @Column(length = 500)
    private String observacao;

    @Column(updatable = false)
    private String cedidaPorCpf;

    private String devolvidaPorCpf;

    public boolean aberta() {
        return devolvidaEm == null;
    }
}
