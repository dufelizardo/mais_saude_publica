package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoDesligamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Verbas rescisórias de um {@link Profissional} (ver docs/rh/MODELO-RH.md, seção 6). O
 * desligamento em si já existe ({@code Profissional.ativo} + {@code dataDesligamento}, ADR-0017)
 * — esta entidade só registra os valores. Campos de valor são <b>informados</b> por quem já
 * calculou fora do sistema (contador/DP), nunca calculados aqui — calcular errado tem risco
 * financeiro e trabalhista real.
 */
@Entity
@Table(name = "TB_CALCULO_RESCISAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CalculoRescisao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoDesligamento tipoDesligamento;

    private BigDecimal avisoPrevio;
    private BigDecimal feriasVencidas;
    private BigDecimal feriasProporcionais;
    private BigDecimal decimoTerceiroProporcional;
    private BigDecimal multaFgts;

    @NotNull
    private BigDecimal total;

    private String documentoTrctUrl;

    public CalculoRescisao(Profissional profissional, TipoDesligamento tipoDesligamento, BigDecimal avisoPrevio,
                            BigDecimal feriasVencidas, BigDecimal feriasProporcionais, BigDecimal decimoTerceiroProporcional,
                            BigDecimal multaFgts, BigDecimal total, String documentoTrctUrl) {
        this.profissional = profissional;
        this.tipoDesligamento = tipoDesligamento;
        this.avisoPrevio = avisoPrevio;
        this.feriasVencidas = feriasVencidas;
        this.feriasProporcionais = feriasProporcionais;
        this.decimoTerceiroProporcional = decimoTerceiroProporcional;
        this.multaFgts = multaFgts;
        this.total = total;
        this.documentoTrctUrl = documentoTrctUrl;
    }
}
