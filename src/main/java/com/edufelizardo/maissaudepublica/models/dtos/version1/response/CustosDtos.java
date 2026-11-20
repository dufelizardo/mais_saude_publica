package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaCusto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoApuracaoCusto;
import com.edufelizardo.maissaudepublica.models.enuns.CriterioRateio;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCentroCusto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** As respostas dos centros de custo, da apuração mensal e dos indicadores de custo, fatias F3 (ADR-0220) e F4 (rateio, ADR-0221). */
public final class CustosDtos {

    private CustosDtos() {
    }

    /**
     * {@code geral}: o centro da unidade sem setor, que recebe o custo sem setor. {@code criterioRateio}: o aplicado (o informado ou o padrão do
     * tipo); nulo no produtivo, que não rateia.
     */
    public record Centro(UUID uuid, String codigo, String nome, TipoCentroCusto tipo, UUID unidadeId, String unidadeNome, UUID setorId, String setorNome,
                         boolean geral, boolean ativo, CriterioRateio criterioRateio, BigDecimal areaM2) {
    }

    public record ApuracaoResumo(UUID uuid, String competencia, SituacaoApuracaoCusto situacao, BigDecimal total, Instant apuradaEm, Instant fechadaEm) {
    }

    /**
     * O custo do centro: {@code valores} e {@code total} são o direto, por categoria; {@code recebido} e {@code enviado}, o rateio; {@code custoFinal},
     * direto + recebido - enviado. {@code internacao}: o produtivo com leito, que vai ao custo por paciente-dia.
     */
    public record LinhaCentro(UUID centroId, String codigo, String nome, TipoCentroCusto tipo, UUID unidadeId, String unidadeNome, String setorNome,
                              Map<CategoriaCusto, BigDecimal> valores, BigDecimal total, BigDecimal recebido, BigDecimal enviado, BigDecimal custoFinal,
                              boolean internacao) {
    }

    /** Uma linha do rateio em degraus: a origem mandou o valor ao destino, pelo critério aplicado. */
    public record Rateio(int ordem, String unidadeNome, UUID origemId, String origemCodigo, String origemNome, UUID destinoId, String destinoCodigo,
                         String destinoNome, CriterioRateio criterio, BigDecimal valor) {
    }

    /**
     * {@code naoAlocado}: o que não achou centro. {@code saidasSemCusto}: as saídas sem preço, fora do total. {@code naoRateado}: o custo de apoio e
     * administração sem destino na unidade; nulo na apuração feita antes do rateio (ADR-0221).
     */
    public record Apuracao(UUID uuid, String competencia, SituacaoApuracaoCusto situacao, BigDecimal total, BigDecimal naoAlocado, int saidasSemCusto,
                           BigDecimal naoRateado, Map<CategoriaCusto, BigDecimal> porCategoria, List<LinhaCentro> centros, List<Rateio> rateios,
                           Instant apuradaEm, Instant fechadaEm) {
    }

    /**
     * O custo da unidade na competência, com o custo por atendimento e por paciente-dia (nulos sem produção). {@code mediaDoTipo}: a média do
     * custo por atendimento das unidades do mesmo tipo; {@code acimaDaMedia}: mais de 30% acima dela.
     */
    public record IndicadorUnidade(UUID unidadeId, String unidadeNome, TipoUnidadeDeSaude tipoUnidade, BigDecimal total,
                                   Map<CategoriaCusto, BigDecimal> porCategoria, int atendimentos, int pacientesDia, BigDecimal custoPorAtendimento,
                                   BigDecimal custoPorPacienteDia, BigDecimal mediaDoTipo, boolean acimaDaMedia) {
    }

    public record Indicadores(String competencia, SituacaoApuracaoCusto situacao, List<IndicadorUnidade> unidades) {
    }
}
