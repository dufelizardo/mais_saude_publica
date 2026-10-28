package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoEncerramentoFrasco;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Frasco multidose aberto (ADR-0148): vale até a validade depois de aberto; as doses que sobram no vencimento viram perda
 * técnica.
 */
@Entity
@Table(name = "TB_FRASCO_ABERTO", indexes = @Index(name = "ix_frasco_aberto_lote", columnList = "lote_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FrascoAberto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private LoteImunobiologico lote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime abertoEm;

    @Column(nullable = false, updatable = false)
    private LocalDateTime validoAte;

    @Column(nullable = false, updatable = false)
    private int dosesNoFrasco;

    @Column(nullable = false)
    private int dosesUsadas;

    private LocalDateTime encerradoEm;

    @Enumerated(EnumType.STRING)
    private MotivoEncerramentoFrasco motivoEncerramento;

    public boolean aberto() {
        return encerradoEm == null;
    }
}
