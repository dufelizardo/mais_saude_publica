package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.CalculoRescisao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDesligamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CalculoRescisaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private TipoDesligamento tipoDesligamento;
    private BigDecimal avisoPrevio;
    private BigDecimal feriasVencidas;
    private BigDecimal feriasProporcionais;
    private BigDecimal decimoTerceiroProporcional;
    private BigDecimal multaFgts;
    private BigDecimal total;
    private String documentoTrctUrl;

    public static CalculoRescisaoResponseDto fromCalculoRescisao(CalculoRescisao calculoRescisao) {
        return new CalculoRescisaoResponseDto(
                calculoRescisao.getUuid(),
                calculoRescisao.getProfissional().getMatricula(),
                calculoRescisao.getProfissional().getNome(),
                calculoRescisao.getTipoDesligamento(),
                calculoRescisao.getAvisoPrevio(),
                calculoRescisao.getFeriasVencidas(),
                calculoRescisao.getFeriasProporcionais(),
                calculoRescisao.getDecimoTerceiroProporcional(),
                calculoRescisao.getMultaFgts(),
                calculoRescisao.getTotal(),
                calculoRescisao.getDocumentoTrctUrl()
        );
    }
}
