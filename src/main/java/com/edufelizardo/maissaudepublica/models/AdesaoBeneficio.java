package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vínculo profissional↔{@link TipoBeneficio} com histórico (ver docs/rh/MODELO-RH.md, seção 2.9).
 * Diferente de {@link Lotacao}: um profissional pode ter várias adesões vigentes ao mesmo tempo
 * (VT + VR + plano de saúde), então não há regra de "no máximo uma vigente por vez" aqui.
 */
@Entity
@Table(name = "TB_ADESAO_BENEFICIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AdesaoBeneficio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_beneficio_id", referencedColumnName = "uuid", nullable = false)
    private TipoBeneficio tipoBeneficio;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    private Integer quantidadeDependentes;

    public AdesaoBeneficio(Profissional profissional, TipoBeneficio tipoBeneficio, LocalDate dataInicio, Integer quantidadeDependentes) {
        this.profissional = profissional;
        this.tipoBeneficio = tipoBeneficio;
        this.dataInicio = dataInicio;
        this.quantidadeDependentes = quantidadeDependentes;
    }
}
