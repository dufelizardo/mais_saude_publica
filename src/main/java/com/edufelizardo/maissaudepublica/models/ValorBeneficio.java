package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Histórico de valor de um {@link TipoBeneficio} (ver docs/rh/MODELO-RH.md, seção 2.8). Mesmo
 * padrão de {@link TabelaSalarial}: vigente = registro com maior {@code dataVigencia <= data}.
 * Reajuste de benefício é independente do dissídio salarial — datas e motivos próprios.
 */
@Entity
@Table(name = "TB_VALOR_BENEFICIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ValorBeneficio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_beneficio_id", referencedColumnName = "uuid", nullable = false)
    private TipoBeneficio tipoBeneficio;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDate dataVigencia;

    private String motivo;

    public ValorBeneficio(TipoBeneficio tipoBeneficio, BigDecimal valor, LocalDate dataVigencia, String motivo) {
        this.tipoBeneficio = tipoBeneficio;
        this.valor = valor;
        this.dataVigencia = dataVigencia;
        this.motivo = motivo;
    }
}
