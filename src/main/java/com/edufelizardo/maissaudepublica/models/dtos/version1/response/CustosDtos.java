package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaCusto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoApuracaoCusto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCentroCusto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** As respostas dos centros de custo, da apuração mensal e dos indicadores de custo, fatia F3 (ADR-0220). */
public final class CustosDtos {

    private CustosDtos() {
    }

    /** {@code geral}: o centro da unidade sem setor, que recebe o custo sem setor. */
    public record Centro(UUID uuid, String codigo, String nome, TipoCentroCusto tipo, UUID unidadeId, String unidadeNome, UUID setorId, String setorNome,
                         boolean geral, boolean ativo) {
    }

    public record ApuracaoResumo(UUID uuid, String competencia, SituacaoApuracaoCusto situacao, BigDecimal total, Instant apuradaEm, Instant fechadaEm) {
    }

    public record LinhaCentro(UUID centroId, String codigo, String nome, TipoCentroCusto tipo, UUID unidadeId, String unidadeNome, String setorNome,
                              Map<CategoriaCusto, BigDecimal> valores, BigDecimal total) {
    }

    /** {@code naoAlocado}: o que não achou centro. {@code saidasSemCusto}: as saídas sem preço, fora do total. */
    public record Apuracao(UUID uuid, String competencia, SituacaoApuracaoCusto situacao, BigDecimal total, BigDecimal naoAlocado, int saidasSemCusto,
                           Map<CategoriaCusto, BigDecimal> porCategoria, List<LinhaCentro> centros, Instant apuradaEm, Instant fechadaEm) {
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
