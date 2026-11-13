package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaTrabalho;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A medida recomendada pela investigação do trabalho (ADR-0208): prazo e a verificação, cumprida ou não. */
@Entity
@Table(name = "TB_MEDIDA_TRABALHO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaTrabalho implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InvestigacaoTrabalho investigacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoMedidaTrabalho tipo;

    @Column(nullable = false, length = 1000, updatable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoMedidaTrabalho situacao;

    private LocalDate verificadaEm;

    @Column(length = 1000)
    private String observacaoVerificacao;

    private String verificadaPorCpf;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
