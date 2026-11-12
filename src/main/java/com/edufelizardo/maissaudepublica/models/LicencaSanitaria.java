package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLicencaSanitaria;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Licença sanitária do estabelecimento, guardada como histórico (ADR-0199). A dispensa (risco I) não tem validade; vencida é calculada pela data. */
@Entity
@Table(name = "TB_LICENCA_SANITARIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LicencaSanitaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @Column(nullable = false, updatable = false)
    private boolean dispensa;

    @Column(nullable = false, updatable = false)
    private LocalDate emitidaEm;

    @Column(updatable = false)
    private LocalDate validade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoLicencaSanitaria situacao;

    @Column(length = 500)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspecao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InspecaoSanitaria inspecao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
