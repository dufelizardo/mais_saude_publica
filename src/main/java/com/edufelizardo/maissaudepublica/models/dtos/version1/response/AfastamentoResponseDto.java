package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AfastamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private TipoAfastamento tipo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusAfastamento status;
    private String observacao;

    public static AfastamentoResponseDto fromAfastamento(Afastamento afastamento) {
        return new AfastamentoResponseDto(
                afastamento.getUuid(),
                afastamento.getProfissional().getMatricula(),
                afastamento.getProfissional().getNome(),
                afastamento.getTipo(),
                afastamento.getDataInicio(),
                afastamento.getDataFim(),
                afastamento.getStatus(),
                afastamento.getObservacao()
        );
    }
}
