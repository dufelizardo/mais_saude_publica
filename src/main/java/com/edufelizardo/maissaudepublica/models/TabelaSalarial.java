package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoTabelaSalarial;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Histórico de valor de um {@link Cargo} (ver docs/rh/MODELO-RH.md, seção 2.3). Valor vigente numa
 * data = registro com maior {@code dataVigencia <= data}. Um dissídio gera um novo registro por
 * cargo afetado — nunca um por profissional; propaga sozinho pra quem estiver lotado nesse cargo.
 */
@Entity
@Table(name = "TB_TABELA_SALARIAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TabelaSalarial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", referencedColumnName = "uuid", nullable = false)
    private Cargo cargo;

    @NotNull
    private BigDecimal valorBase;

    @NotNull
    private LocalDate dataVigencia;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MotivoTabelaSalarial motivo;

    public TabelaSalarial(Cargo cargo, BigDecimal valorBase, LocalDate dataVigencia, MotivoTabelaSalarial motivo) {
        this.cargo = cargo;
        this.valorBase = valorBase;
        this.dataVigencia = dataVigencia;
        this.motivo = motivo;
    }
}
