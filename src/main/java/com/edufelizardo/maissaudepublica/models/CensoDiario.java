package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Censo hospitalar diário fechado (ADR-0185): os números do dia de uma unidade, calculados das internações e dos eventos de leito
 * e guardados no fechamento. Corrigir é reabrir com motivo e fechar outra versão; a reaberta fica guardada.
 */
@Entity
@Table(name = "TB_CENSO_DIARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CensoDiario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(nullable = false, updatable = false)
    private Integer versao;

    @Column(nullable = false, updatable = false)
    private Integer leitosExistentes;

    @Column(nullable = false, updatable = false)
    private Integer leitosBloqueados;

    @Column(nullable = false, updatable = false)
    private Integer leitosOperacionais;

    /** Internados no começo do dia (00h). */
    @Column(nullable = false, updatable = false)
    private Integer internadosInicio;

    @Column(nullable = false, updatable = false)
    private Integer admissoes;

    @Column(nullable = false, updatable = false)
    private Integer altasMelhorado;

    @Column(name = "altas_a_pedido", nullable = false, updatable = false)
    private Integer altasAPedido;

    @Column(nullable = false, updatable = false)
    private Integer altasTransferencia;

    @Column(nullable = false, updatable = false)
    private Integer evasoes;

    @Column(nullable = false, updatable = false)
    private Integer obitos;

    @Column(nullable = false, updatable = false)
    private Integer transferenciasInternas;

    /** Internados no fim do dia (24h): o censo nominal. */
    @Column(nullable = false, updatable = false)
    private Integer internadosFim;

    /** Internados às 24h mais os que entraram e saíram no mesmo dia. */
    @Column(nullable = false, updatable = false)
    private Integer pacienteDia;

    @Column(precision = 6, scale = 1, updatable = false)
    private BigDecimal taxaOcupacao;

    @Column(precision = 8, scale = 1, updatable = false)
    private BigDecimal mediaPermanencia;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant fechadoEm;

    @Column(updatable = false)
    private String fechadoPorCpf;

    private boolean reaberto;

    private Instant reabertoEm;

    private String reabertoPorCpf;

    @Column(length = 1000)
    private String motivoReabertura;
}
