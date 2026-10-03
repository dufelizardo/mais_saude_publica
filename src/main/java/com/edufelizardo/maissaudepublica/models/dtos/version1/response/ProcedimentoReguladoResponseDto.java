package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProcedimentoRegulado;
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
public class ProcedimentoReguladoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private TipoProcedimentoRegulado tipo;
    private boolean ativo;

    public static ProcedimentoReguladoResponseDto fromProcedimento(ProcedimentoRegulado p) {
        return new ProcedimentoReguladoResponseDto(p.getUuid(), p.getNome(), p.getTipo(), p.isAtivo());
    }
}
