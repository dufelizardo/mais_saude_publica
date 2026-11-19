package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ExecucaoManutencao;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeManutencao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemServico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoManutencao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas da manutenção, fatia PM2 (ADR-0216). */
public final class ManutencaoDtos {

    private ManutencaoDtos() {
    }

    public record Plano(UUID uuid, UUID bemId, String numeroTombamento, String bemDescricao, String unidadeNome, TipoManutencao tipo, int periodicidadeDias,
                        LocalDate ultimaExecucao, LocalDate proximaEm, String descricao, boolean ativo, boolean vencido) {
    }

    /** A ata ou o contrato vigente com item de serviço, para a execução por contrato. */
    public record ContratoServico(UUID uuid, String numero, String fornecedorNome, LocalDate fim) {
    }

    public record Peca(UUID uuid, UUID materialId, String descricao, int quantidade, BigDecimal custoUnitario) {
    }

    /** {@code diasParado}: da parada até a conclusão (ou até hoje, se ainda aberta). {@code custoTotal}: mão de obra mais as peças com custo. */
    public record Ordem(UUID uuid, String numero, UUID bemId, String numeroTombamento, String bemDescricao, UUID unidadeId, String unidadeNome,
                        TipoManutencao tipo, UUID planoId, String defeito, boolean bemParado, LocalDate paradoDesde, LocalDate abertaEm,
                        PrioridadeManutencao prioridade, SituacaoOrdemServico situacao, ExecucaoManutencao execucao, UUID instrumentoId,
                        String instrumentoNumero, String tecnico, LocalDate iniciadaEm, LocalDate concluidaEm, String servicoRealizado,
                        BigDecimal custoMaoDeObra, BigDecimal custoPecas, BigDecimal custoTotal, Long diasParado, String certificado,
                        LocalDate calibracaoValidaAte, String motivoCancelamento, List<Peca> pecas) {
    }

    /** A manutenção na ficha do bem: os planos, as ordens, o custo acumulado, os dias parado e a calibração. */
    public record DoBem(List<Plano> planos, List<Ordem> ordens, BigDecimal custoTotal, long diasParado, LocalDate calibracaoValidaAte,
                        boolean calibracaoVencida) {
    }

    /**
     * {@code planosVencendo}: vencem nos próximos 30 dias ou já venceram. {@code calibracoesVencidas}: certificado vencido ou plano vencido.
     * {@code equipamentosParados}: ordens abertas de equipamento médico-hospitalar parado há mais de 7 dias (ADR-0217, Sala de Situação).
     */
    public record Resumo(long abertas, long emExecucao, long paradas, List<Plano> planosVencendo, List<Plano> calibracoesVencidas,
                         List<Ordem> equipamentosParados) {
    }
}
