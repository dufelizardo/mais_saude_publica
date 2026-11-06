package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A sala de vacina no dia (ADR-0147): as doses aplicadas na unidade e a contagem por imunobiológico. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VacinacaoDoDiaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String unidadeNome;
    private LocalDate data;
    private long total;
    private List<ContagemProducaoDto> porImunobiologico;
    private List<RegistroVacinacaoDto> registros;
}
