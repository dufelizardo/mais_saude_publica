package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Consumo que o sistema tentou lançar e não achou saldo (ADR-0183), por exemplo o tubo da coleta: a coleta não para, e o alerta
 * fica para o almoxarife acertar a entrada ou o inventário e marcar como conferido.
 */
@Entity
@Table(name = "TB_CONSUMO_SEM_SALDO", indexes = @Index(name = "ix_consumo_sem_saldo_unidade", columnList = "unidade_id, conferido_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConsumoSemSaldo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private MaterialEstoque material;

    @Column(nullable = false, updatable = false)
    private Integer quantidadeFaltante;

    /** De onde veio o consumo, por exemplo "Coleta do pedido 3f2a…, amostra AM261109-K7PQ". */
    @Column(nullable = false, length = 300, updatable = false)
    private String origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    private Instant conferidoEm;

    private String conferidoPorCpf;
}
