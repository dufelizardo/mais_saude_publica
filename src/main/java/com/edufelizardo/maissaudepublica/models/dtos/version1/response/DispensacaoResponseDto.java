package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Dispensacao;
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
public class DispensacaoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID loteUuid;
    private String loteNumeroLote;
    private String medicamentoNome;
    private UUID pacienteUuid;
    private String pacienteNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private UUID consultaUuid;
    private Integer quantidade;
    private LocalDateTime dataHora;

    public static DispensacaoResponseDto fromDispensacao(Dispensacao dispensacao) {
        return new DispensacaoResponseDto(
                dispensacao.getUuid(),
                dispensacao.getLote().getUuid(),
                dispensacao.getLote().getNumeroLote(),
                dispensacao.getLote().getMedicamento().getNome(),
                dispensacao.getPaciente().getUuid(),
                dispensacao.getPaciente().getNome(),
                dispensacao.getProfissional().getMatricula(),
                dispensacao.getProfissional().getNome(),
                dispensacao.getConsulta() != null ? dispensacao.getConsulta().getUuid() : null,
                dispensacao.getQuantidade(),
                dispensacao.getDataHora()
        );
    }
}
