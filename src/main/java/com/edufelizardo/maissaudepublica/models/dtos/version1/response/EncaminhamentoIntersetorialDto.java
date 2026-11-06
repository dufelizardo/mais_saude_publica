package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoExterna;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Encaminhamento a uma instituição externa (ADR-0154): para onde, por quê, o prazo e o que voltou. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EncaminhamentoIntersetorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID instituicaoId;
    private String instituicaoNome;
    private SetorIntersetorial setor;
    private TipoInstituicaoExterna tipo;
    private String instituicaoTelefone;
    private String motivo;
    private LocalDate enviadoEm;
    private LocalDate retornoEsperadoEm;
    private SituacaoEncaminhamentoIntersetorial situacao;
    /** Em aberto depois do prazo esperado. */
    private boolean semRetorno;
    private LocalDate recebidoEm;
    private LocalDate retornoEm;
    private String retorno;
    private String motivoCancelamento;
    private String profissionalNome;
}
