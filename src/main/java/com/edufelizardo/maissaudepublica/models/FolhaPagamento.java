package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Folha de pagamento mensal de um {@link Profissional} (ver docs/rh/MODELO-RH.md, seção 7).
 * Consome conceitualmente salário efetivo + benefícios + ponto + afastamentos do mês, mas nenhuma
 * dessas composições é calculada aqui — proventos/descontos/encargos/total são <b>informados</b>,
 * mesmo padrão de {@link CalculoRescisao}. No máximo uma folha por profissional por competência.
 */
@Entity
@Table(name = "TB_FOLHA_PAGAMENTO", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profissional_id", "competencia"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FolhaPagamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotBlank
    private String competencia;

    @NotNull
    private BigDecimal proventos;

    @NotNull
    private BigDecimal descontos;

    @NotNull
    private BigDecimal encargos;

    @NotNull
    private BigDecimal total;

    public FolhaPagamento(Profissional profissional, String competencia, BigDecimal proventos,
                           BigDecimal descontos, BigDecimal encargos, BigDecimal total) {
        this.profissional = profissional;
        this.competencia = competencia;
        this.proventos = proventos;
        this.descontos = descontos;
        this.encargos = encargos;
        this.total = total;
    }
}
