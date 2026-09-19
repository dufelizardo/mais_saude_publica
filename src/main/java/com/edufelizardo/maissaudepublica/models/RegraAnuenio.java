package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Regra de progressão automática por tempo de serviço, uma por {@link CategoriaSalarial} (ver
 * docs/rh/MODELO-RH.md, seção 2.5). Só a regra — não é um lançamento gravado por profissional/ano;
 * o cálculo do valor de anuênio fica pra quando a Folha for desenhada, não faz parte desta fatia.
 */
@Entity
@Table(name = "TB_REGRA_ANUENIO", uniqueConstraints = {
        @UniqueConstraint(columnNames = "categoria_salarial_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegraAnuenio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_salarial_id", referencedColumnName = "uuid", nullable = false)
    private CategoriaSalarial categoria;

    @NotNull
    private BigDecimal percentualPorAno;

    private Integer tetoAnos;

    public RegraAnuenio(CategoriaSalarial categoria, BigDecimal percentualPorAno, Integer tetoAnos) {
        this.categoria = categoria;
        this.percentualPorAno = percentualPorAno;
        this.tetoAnos = tetoAnos;
    }
}
