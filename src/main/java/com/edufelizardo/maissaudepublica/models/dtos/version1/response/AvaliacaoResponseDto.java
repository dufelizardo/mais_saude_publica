package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Avaliacao;
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
public class AvaliacaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String cicloNome;
    private String avaliador;
    private BigDecimal nota;
    private String observacao;

    public static AvaliacaoResponseDto fromAvaliacao(Avaliacao avaliacao) {
        return new AvaliacaoResponseDto(
                avaliacao.getUuid(),
                avaliacao.getProfissional().getMatricula(),
                avaliacao.getProfissional().getNome(),
                avaliacao.getCiclo().getNome(),
                avaliacao.getAvaliador(),
                avaliacao.getNota(),
                avaliacao.getObservacao()
        );
    }
}
