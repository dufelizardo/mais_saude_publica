package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Uma linha de distribuição da produção (por tipo, especialidade, categoria, faixa etária ou sexo), ADR-0143. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ContagemProducaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String chave;
    private String rotulo;
    private long quantidade;
}
