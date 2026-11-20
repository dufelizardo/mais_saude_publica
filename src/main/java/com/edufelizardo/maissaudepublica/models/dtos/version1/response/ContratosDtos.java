package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoItemContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAditivoContratual;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstrumentoContratual;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas da contratação, das atas e dos contratos, fatia CP2 de Compras (ADR-0213). */
public final class ContratosDtos {

    private ContratosDtos() {
    }

    public record SolicitacaoResumo(UUID uuid, String numero, String unidadeNome) {
    }

    public record InstrumentoResumo(UUID uuid, TipoInstrumentoContratual tipo, String numero, String fornecedorNome) {
    }

    public record ItemContratacao(UUID uuid, int ordem, TipoItemCompra tipo, String descricao, String unidadeMedida, int quantidade,
                                  BigDecimal precoReferencia, ResultadoItemContratacao resultado, UUID fornecedorId, String fornecedorNome,
                                  BigDecimal precoUnitario, String justificativaPreco, BigDecimal valor) {
    }

    public record Contratacao(UUID uuid, String numeroProcesso, ModalidadeContratacao modalidade, String fundamentoLegal, String ataOrigem,
                              String objeto, SituacaoContratacao situacao, LocalDate homologadaEm, String motivoCancelamento,
                              List<SolicitacaoResumo> solicitacoes, List<ItemContratacao> itens, BigDecimal valorReferencia,
                              BigDecimal valorAdjudicado, List<InstrumentoResumo> instrumentos) {
    }

    /** {@code saldoBaixo}: menos de 20% da quantidade. */
    public record ItemInstrumento(UUID uuid, TipoItemCompra tipo, UUID materialId, UUID medicamentoId, String descricao, String unidadeMedida,
                                  int quantidadeInicial, int quantidade, int saldo, BigDecimal precoUnitario, boolean saldoBaixo) {
    }

    public record Aditivo(UUID uuid, int numero, TipoAditivoContratual tipo, LocalDate fimAnterior, LocalDate novoFim, UUID itemId,
                          String itemDescricao, Integer quantidade, BigDecimal precoAnterior, BigDecimal novoPreco, String justificativa,
                          LocalDate assinadoEm) {
    }

    /**
     * A ata ou o contrato. {@code diasParaVencer} negativo: vencido. Os percentuais são os acréscimos e as supressões sobre o valor inicial
     * (art. 125). {@code alertas}: vigência, saldo e situação do fornecedor. {@code ordensEmAberto}: ordens de fornecimento à espera de
     * entrega (ADR-0214).
     */
    public record Instrumento(UUID uuid, TipoInstrumentoContratual tipo, String numero, UUID contratacaoId, String numeroProcesso,
                              UUID fornecedorId, String fornecedorNome, LocalDate inicio, LocalDate fim, boolean servicoContinuo, boolean vigente,
                              long diasParaVencer, BigDecimal valorInicial, BigDecimal valorAtual, BigDecimal percentualAcrescido,
                              BigDecimal percentualSuprimido, String gestorMatricula, String gestorNome, String fiscalMatricula, String fiscalNome,
                              List<ItemInstrumento> itens, List<Aditivo> aditivos, List<String> alertas, long ordensEmAberto) {
    }
}
