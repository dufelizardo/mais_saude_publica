package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Composição derivada pra exibição — não persiste nada, não é a Folha de pagamento (ver
 * docs/rh/MODELO-RH.md seção 2.4). Recalculada a cada consulta a partir da Lotação vigente,
 * TabelaSalarial vigente do cargo, RegraAnuenio da categoria e AjusteIndividual vigentes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComposicaoRemuneratoriaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String profissionalMatricula;
    private String profissionalNome;
    private String cargoNome;
    private String categoriaNome;
    private BigDecimal valorBase;
    private Integer anosCompletos;
    private BigDecimal percentualAnuenio;
    private BigDecimal valorAnuenio;
    private List<AjusteIndividualResponseDto> ajustesIndividuaisVigentes;
    private BigDecimal totalAjustesIndividuais;
    private BigDecimal total;
}
