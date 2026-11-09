package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.RealizacaoExameImagem;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Realização de um exame de imagem (ADR-0182). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RealizacaoExameImagemDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID itemId;
    private String exameNome;
    private UUID unidadeId;
    private String unidadeNome;
    private Instant realizadaEm;
    private String tecnicoMatricula;
    private String tecnicoNome;
    private String equipamento;
    private boolean contraste;
    private String contrasteDescricao;
    private String observacao;

    public static RealizacaoExameImagemDto fromRealizacao(RealizacaoExameImagem r) {
        return new RealizacaoExameImagemDto(r.getUuid(), r.getItem().getUuid(), r.getItem().getExame().getNome(), r.getUnidade().getUuid(),
                r.getUnidade().getNome(), r.getRealizadaEm(), r.getTecnico().getMatricula(), r.getTecnico().getNome(), r.getEquipamento(),
                r.isContraste(), r.getContrasteDescricao(), r.getObservacao());
    }
}
