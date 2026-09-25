package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ProcessoAdministrativo;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcessoAdministrativoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID capacidadeUuid;
    private String capacidadeCodigo;
    private String capacidadeNome;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;

    public static ProcessoAdministrativoResponseDto fromProcessoAdministrativo(ProcessoAdministrativo processo) {
        return new ProcessoAdministrativoResponseDto(
                processo.getUuid(),
                processo.getCapacidade().getUuid(),
                processo.getCapacidade().getCodigo(),
                processo.getCapacidade().getNome(),
                processo.getCodigo(),
                processo.getNome(),
                processo.getDescricao(),
                processo.isAtivo()
        );
    }
}
