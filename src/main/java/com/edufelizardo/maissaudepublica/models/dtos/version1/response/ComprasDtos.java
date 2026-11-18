package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FonteCotacao;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeCompra;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCertidaoFornecedor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSancaoFornecedor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas de Compras, fatia CP1 (ADR-0212): fornecedores, solicitações de compra e pesquisa de preços. */
public final class ComprasDtos {

    private ComprasDtos() {
    }

    public record Certidao(UUID uuid, TipoCertidaoFornecedor tipo, String numero, LocalDate emitidaEm, LocalDate validaAte, boolean vencida) {
    }

    public record Sancao(UUID uuid, TipoSancaoFornecedor tipo, LocalDate inicio, LocalDate fim, BigDecimal valor, String motivo, String processo,
                         boolean vigente, LocalDate revogadaEm, String motivoRevogacao) {
    }

    /**
     * O fornecedor. {@code impedido}: impedimento ou inidoneidade vigente. {@code certidoesPendentes}: os tipos exigidos sem certidão válida
     * hoje (a pessoa física não precisa do FGTS). {@code regular}: ativo, sem impedimento e sem pendência.
     */
    public record Fornecedor(UUID uuid, String documento, boolean pessoaJuridica, String razaoSocial, String nomeFantasia, String email,
                             String telefone, String endereco, boolean ativo, boolean impedido, boolean regular,
                             List<TipoCertidaoFornecedor> certidoesPendentes, List<Certidao> certidoes, List<Sancao> sancoes) {
    }

    public record Cotacao(UUID uuid, FonteCotacao fonte, UUID fornecedorId, String fornecedorNome, String referencia, BigDecimal precoUnitario,
                          LocalDate cotadoEm) {
    }

    /** O item; {@code mediana} é a das cotações de agora; {@code precoReferencia}, a fixada ao concluir a pesquisa. */
    public record Item(UUID uuid, int ordem, TipoItemCompra tipo, UUID materialId, UUID medicamentoId, String descricao, String unidadeMedida,
                       int quantidade, List<Cotacao> cotacoes, BigDecimal mediana, BigDecimal precoReferencia, BigDecimal valorEstimado,
                       String justificativaCotacao) {
    }

    public record Solicitacao(UUID uuid, String numero, UUID unidadeId, String unidadeNome, UUID setorId, String setorNome, String justificativa,
                              PrioridadeCompra prioridade, SituacaoSolicitacaoCompra situacao, String motivo, List<Item> itens,
                              BigDecimal valorEstimado, Instant registradoEm, Instant enviadaEm, Instant analisadaEm, Instant pesquisaConcluidaEm,
                              UUID contratacaoId) {
    }

    /** Material abaixo do mínimo na unidade, ainda sem solicitação em andamento; a sugestão repõe até o dobro do mínimo. */
    public record Sugestao(UUID materialId, String codigo, String nome, String unidadeMedida, int saldo, int minimo, int quantidadeSugerida) {
    }
}
