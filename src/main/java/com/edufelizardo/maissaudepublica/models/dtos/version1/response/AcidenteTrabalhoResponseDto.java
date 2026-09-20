package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AcidenteTrabalho;
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
public class AcidenteTrabalhoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private String descricao;
    private boolean catEmitida;
    private String catUrl;
    private Integer diasAfastamento;

    public static AcidenteTrabalhoResponseDto fromAcidenteTrabalho(AcidenteTrabalho acidente) {
        return new AcidenteTrabalhoResponseDto(
                acidente.getUuid(),
                acidente.getProfissional().getMatricula(),
                acidente.getProfissional().getNome(),
                acidente.getDataHora(),
                acidente.getDescricao(),
                acidente.isCatEmitida(),
                acidente.getCatUrl(),
                acidente.getDiasAfastamento()
        );
    }
}
