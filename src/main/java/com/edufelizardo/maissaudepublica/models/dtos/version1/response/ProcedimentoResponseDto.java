package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcedimentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID consultaUuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private String tipo;
    private String descricao;
    private LocalDateTime dataRealizacao;
    private StatusProcedimento status;

    public static ProcedimentoResponseDto fromProcedimento(Procedimento procedimento) {
        return new ProcedimentoResponseDto(
                procedimento.getUuid(),
                procedimento.getConsulta().getUuid(),
                procedimento.getProfissional().getMatricula(),
                procedimento.getProfissional().getNome(),
                procedimento.getTipo(),
                procedimento.getDescricao(),
                procedimento.getDataRealizacao(),
                procedimento.getStatus()
        );
    }
}
