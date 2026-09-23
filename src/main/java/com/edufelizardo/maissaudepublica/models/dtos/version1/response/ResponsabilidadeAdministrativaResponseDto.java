package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ResponsabilidadeAdministrativa;
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
public class ResponsabilidadeAdministrativaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private UUID setorUuid;
    private String setorNome;
    private String tipo;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public static ResponsabilidadeAdministrativaResponseDto fromResponsabilidadeAdministrativa(
            ResponsabilidadeAdministrativa responsabilidade) {
        return new ResponsabilidadeAdministrativaResponseDto(
                responsabilidade.getUuid(),
                responsabilidade.getProfissional().getMatricula(),
                responsabilidade.getProfissional().getNome(),
                responsabilidade.getSetor().getUuid(),
                responsabilidade.getSetor().getNome(),
                responsabilidade.getTipo(),
                responsabilidade.getDescricao(),
                responsabilidade.getDataInicio(),
                responsabilidade.getDataFim()
        );
    }
}
