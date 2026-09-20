package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.FolhaPagamento;
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
public class FolhaPagamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String competencia;
    private BigDecimal proventos;
    private BigDecimal descontos;
    private BigDecimal encargos;
    private BigDecimal total;

    public static FolhaPagamentoResponseDto fromFolhaPagamento(FolhaPagamento folhaPagamento) {
        return new FolhaPagamentoResponseDto(
                folhaPagamento.getUuid(),
                folhaPagamento.getProfissional().getMatricula(),
                folhaPagamento.getProfissional().getNome(),
                folhaPagamento.getCompetencia(),
                folhaPagamento.getProventos(),
                folhaPagamento.getDescontos(),
                folhaPagamento.getEncargos(),
                folhaPagamento.getTotal()
        );
    }
}
