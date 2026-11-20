package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoExterna;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * O painel da rede intersetorial, fatia IS2 (ADR-0211). Sem paciente e sem motivo: o encaminhamento de violência e de
 * saúde mental é sigiloso, e para cobrar a instituição bastam o número, as datas, a origem e a unidade.
 */
public final class PainelIntersetorialDtos {

    private PainelIntersetorialDtos() {
    }

    /**
     * Os números do período (pelo envio) e a situação de hoje: {@code emAberto} e {@code semRetorno} contam todo
     * encaminhamento ainda em aberto, de qualquer data, porque é o que se cobra da instituição.
     */
    public record PainelDto(
            LocalDate inicio,
            LocalDate fim,
            long enviados,
            long emAberto,
            long semRetorno,
            long concluidos,
            long recusados,
            long cancelados,
            Double taxaResolucao,
            Double tempoMedioRetornoDias,
            List<InstituicaoDto> instituicoes) {
    }

    public record InstituicaoDto(
            UUID instituicaoId,
            String nome,
            SetorIntersetorial setor,
            TipoInstituicaoExterna tipo,
            String telefone,
            String responsavel,
            boolean ativa,
            long enviados,
            long emAberto,
            long semRetorno,
            long concluidos,
            long recusados,
            long cancelados,
            Double taxaResolucao,
            Double tempoMedioRetornoDias,
            Map<DominioOrigemEncaminhamento, Long> enviadosPorOrigem) {
    }

    /** Um encaminhamento sem retorno, para cobrar a instituição: o número é o começo do identificador. */
    public record SemRetornoDto(
            UUID encaminhamentoId,
            String numero,
            DominioOrigemEncaminhamento origem,
            String unidadeNome,
            SituacaoEncaminhamentoIntersetorial situacao,
            LocalDate enviadoEm,
            LocalDate recebidoEm,
            LocalDate retornoEsperadoEm,
            long diasDeAtraso) {
    }

    public record CobrancaDto(InstituicaoExternaDto instituicao, List<SemRetornoDto> semRetorno) {
    }
}
