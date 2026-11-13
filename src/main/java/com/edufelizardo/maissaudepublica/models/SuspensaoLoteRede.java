package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSuspensaoLote;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Suspensão do lote na rede (ADR-0206): o imunobiológico e o número do lote ficam sob suspeita em todas as salas de vacina até a coordenação da rede liberar ou inutilizar. */
@Entity
@Table(name = "TB_SUSPENSAO_LOTE_REDE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SuspensaoLoteRede implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imunobiologico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Imunobiologico imunobiologico;

    @Column(nullable = false, length = 40, updatable = false)
    private String numeroLote;

    @Column(nullable = false, length = 1000, updatable = false)
    private String sinal;

    @Column(nullable = false, length = 1000, updatable = false)
    private String motivo;

    @Column(length = 2000, updatable = false)
    private String notificacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoSuspensaoLote situacao;

    @Column(nullable = false, updatable = false)
    private Instant suspensaEm;

    @Column(updatable = false)
    private String suspensaPorCpf;

    private Instant decididaEm;

    private String decididaPorCpf;

    @Column(length = 1000)
    private String parecer;
}
