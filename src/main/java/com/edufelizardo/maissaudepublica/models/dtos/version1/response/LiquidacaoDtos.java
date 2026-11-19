package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.BeneficiarioDiariaTfd;
import com.edufelizardo.maissaudepublica.models.enuns.FonteRecursoSaude;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemLiquidacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas da liquidação, do pagamento, das diárias do TFD, dos restos a pagar e da execução, fatia F2 (ADR-0219). */
public final class LiquidacaoDtos {

    private LiquidacaoDtos() {
    }

    /** O atesto à espera da liquidação. {@code empenhoId}: o empenho da ordem, quando ela tem um (a liquidação usa só ele). */
    public record Pendencia(OrigemLiquidacao origem, UUID origemId, String descricao, UUID fornecedorId, String fornecedorNome, UUID instrumentoId,
                            String instrumentoNumero, UUID empenhoId, String empenhoNumero, BigDecimal valor, LocalDate atestadoEm, String unidadeNome) {
    }

    public record Liquidacao(UUID uuid, OrigemLiquidacao origem, UUID origemId, String descricao, UUID empenhoId, String empenhoNumero, String fornecedorNome,
                             FonteRecursoSaude fonte, String notaFiscal, LocalDate notaEmitidaEm, BigDecimal valorBruto, BigDecimal iss, BigDecimal irrf,
                             BigDecimal inss, BigDecimal outrasRetencoes, BigDecimal valorLiquido, LocalDate liquidadaEm, LocalDate vencimento,
                             boolean vencida, boolean paga, LocalDate pagaEm, String ordemBancaria, String justificativa, String justificativaForaDeOrdem) {
    }

    public record Diaria(UUID uuid, UUID solicitacaoId, String pacienteNome, String destino, BeneficiarioDiariaTfd beneficiario, String beneficiarioNome,
                         UUID credorId, String credorNome, int dias, BigDecimal valorDiaria, BigDecimal valorTotal, LocalDate concedidaEm, boolean liquidada) {
    }

    /** {@code processado}: liquidado e não pago. {@code naoProcessado}: empenhado e não liquidado. */
    public record RestoAPagar(UUID empenhoId, String numero, int exercicio, String fornecedorNome, BigDecimal processado, BigDecimal naoProcessado) {
    }

    public record ExecucaoDotacao(UUID dotacaoId, String rotulo, FonteRecursoSaude fonte, BigDecimal autorizado, BigDecimal empenhado,
                                  BigDecimal liquidado, BigDecimal pago) {
    }

    public record ExecucaoFonte(FonteRecursoSaude fonte, BigDecimal autorizado, BigDecimal empenhado, BigDecimal liquidado, BigDecimal pago) {
    }

    /** {@code prazoMedioPagamentoDias}: do atesto ao pagamento, nas liquidações pagas do exercício com atesto na plataforma. */
    public record Execucao(int exercicio, List<ExecucaoDotacao> dotacoes, List<ExecucaoFonte> fontes, long aLiquidarQuantidade, BigDecimal aLiquidarValor,
                           long vencidasQuantidade, BigDecimal vencidasValor, Double prazoMedioPagamentoDias) {
    }
}
