package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoPlano;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** O plano (uma versão) ativo numa emergência (ADR-0157); os passos ficam em {@link RegistroAtivacaoPlano}. */
@Entity
@Table(name = "TB_ATIVACAO_PLANO", indexes = @Index(name = "ix_ativacao_plano_emergencia", columnList = "emergencia_id, situacao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtivacaoPlano implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PlanoContingencia plano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoAtivacaoPlano situacao;

    @Column(nullable = false, updatable = false)
    private Instant ativadaEm;

    private Instant desativadaEm;
}
