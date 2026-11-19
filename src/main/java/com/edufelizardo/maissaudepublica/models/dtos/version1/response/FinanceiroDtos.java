package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FonteRecursoSaude;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemFornecimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlteracaoDotacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEmpenho;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentoEmpenho;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas do Financeiro, fatia F1 (ADR-0218). */
public final class FinanceiroDtos {

    private FinanceiroDtos() {
    }

    public record Alteracao(UUID uuid, TipoAlteracaoDotacao tipo, BigDecimal valor, String ato, LocalDate data, String motivo) {
    }

    /** {@code autorizado}: a lei mais as suplementações menos as anulações. {@code disponivel}: o autorizado menos o empenhado. */
    public record Dotacao(UUID uuid, int exercicio, String programaTrabalho, String descricao, String naturezaDespesa, FonteRecursoSaude fonte,
                          String fonteCodigo, BigDecimal valorInicial, BigDecimal suplementado, BigDecimal anulado, BigDecimal autorizado,
                          BigDecimal empenhado, BigDecimal disponivel, List<Alteracao> alteracoes) {
    }

    public record Movimento(UUID uuid, TipoMovimentoEmpenho tipo, BigDecimal valor, LocalDate data, String motivo) {
    }

    public record OrdemDoEmpenho(UUID uuid, String numero, SituacaoOrdemFornecimento situacao, BigDecimal valor) {
    }

    /**
     * {@code valorAtual}: o inicial mais os reforços menos as anulações. {@code comprometido}: as ordens de fornecimento (a cancelada conta
     * só o que foi aceito). {@code saldo}: o atual menos o comprometido.
     */
    public record Empenho(UUID uuid, String numero, int exercicio, TipoEmpenho tipo, UUID dotacaoId, String dotacaoRotulo, UUID fornecedorId,
                          String fornecedorNome, UUID instrumentoId, String instrumentoNumero, BigDecimal valorInicial, BigDecimal reforcado,
                          BigDecimal anulado, BigDecimal valorAtual, BigDecimal comprometido, BigDecimal saldo, LocalDate data, String historico,
                          List<Movimento> movimentos, List<OrdemDoEmpenho> ordens) {
    }
}
