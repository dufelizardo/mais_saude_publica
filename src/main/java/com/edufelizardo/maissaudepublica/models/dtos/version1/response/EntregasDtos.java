package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaFornecedor;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemFornecimento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRecebimentoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoOcorrenciaFornecedor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** As respostas das ordens de fornecimento, do recebimento, das ocorrências e do painel de Compras, fatia CP3 (ADR-0214). */
public final class EntregasDtos {

    private EntregasDtos() {
    }

    /**
     * {@code emRecebimento}: entregue em recebimento ainda provisório. {@code pendente}: o que falta entregar. {@code pedeLote}: o
     * medicamento e o material que controla lote pedem o lote e a validade na entrega.
     */
    public record ItemOrdem(UUID uuid, UUID itemInstrumentoId, TipoItemCompra tipo, String descricao, String unidadeMedida, int quantidade,
                            int aceita, int emRecebimento, int pendente, BigDecimal precoUnitario, boolean pedeLote) {
    }

    public record ItemRecebimento(UUID uuid, UUID itemOrdemId, String descricao, int entregue, Integer aceita, Integer recusada, String motivoRecusa,
                                  String numeroLote, LocalDate validade) {
    }

    public record Recebimento(UUID uuid, String notaFiscal, LocalDate notaEmitidaEm, LocalDate recebidoEm, SituacaoRecebimentoCompra situacao,
                              LocalDate definitivoEm, String observacao, List<ItemRecebimento> itens) {
    }

    public record Ocorrencia(UUID uuid, UUID fornecedorId, String fornecedorNome, UUID ordemId, String ordemNumero, TipoOcorrenciaFornecedor tipo,
                             String descricao, LocalDate registradaEm, SituacaoOcorrenciaFornecedor situacao, String resposta, LocalDate encerradaEm,
                             UUID sancaoId) {
    }

    public record Ordem(UUID uuid, String numero, UUID instrumentoId, String instrumentoNumero, UUID fornecedorId, String fornecedorNome,
                        UUID unidadeId, String unidadeNome, String empenho, LocalDate emitidaEm, LocalDate prazoEm, boolean atrasada, long diasDeAtraso,
                        SituacaoOrdemFornecimento situacao, String motivoCancelamento, BigDecimal valor, List<ItemOrdem> itens,
                        List<Recebimento> recebimentos, List<Ocorrencia> ocorrencias) {
    }

    /** O desempenho do fornecedor nas entregas definitivas do período. */
    public record DesempenhoFornecedor(UUID fornecedorId, String fornecedorNome, long ordens, long entregas, Double percentualNoPrazo,
                                       Double prazoMedioDias, long quantidadeRecusada, long ocorrencias) {
    }

    public record OrdemAtrasada(UUID ordemId, String numero, String fornecedorNome, String unidadeNome, LocalDate prazoEm, long diasDeAtraso) {
    }

    public record Painel(LocalDate inicio, LocalDate fim, Map<SituacaoSolicitacaoCompra, Long> solicitacoesPorSituacao, BigDecimal valorContratado,
                         long ordensEmAberto, long ordensEmAtraso, long contratosVencendo, long itensComSaldoBaixo, long ocorrenciasAbertas,
                         List<OrdemAtrasada> atrasadas, List<DesempenhoFornecedor> fornecedores) {
    }
}
