package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoAjusteIndividual;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resíduo do salário: só concessões pontuais e individuais (ver docs/rh/MODELO-RH.md, seção 2.6)
 * — nem regra automática (isso é {@link RegraAnuenio}), nem tabela de cargo (isso é
 * {@link TabelaSalarial}). Pode haver mais de um vigente ao mesmo tempo para o mesmo profissional,
 * diferente de {@link Lotacao}.
 */
@Entity
@Table(name = "TB_AJUSTE_INDIVIDUAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AjusteIndividual implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MotivoAjusteIndividual motivo;

    private String referencia;

    public AjusteIndividual(Profissional profissional, BigDecimal valor, LocalDate dataInicio,
                             LocalDate dataFim, MotivoAjusteIndividual motivo, String referencia) {
        this.profissional = profissional;
        this.valor = valor;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.motivo = motivo;
        this.referencia = referencia;
    }
}
