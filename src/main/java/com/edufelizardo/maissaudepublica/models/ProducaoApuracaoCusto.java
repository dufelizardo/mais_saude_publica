package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A produção da unidade na competência, guardada com a apuração (ADR-0220): os atendimentos concluídos e os pacientes-dia do censo. */
@Entity
@Table(name = "TB_PRODUCAO_APURACAO_CUSTO", uniqueConstraints = @UniqueConstraint(name = "uk_producao_apuracao_custo", columnNames = {"apuracao_id", "unidade_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProducaoApuracaoCusto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apuracao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ApuracaoCusto apuracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Column(nullable = false, updatable = false)
    private Integer atendimentos;

    @Column(nullable = false, updatable = false)
    private Integer pacientesDia;
}
