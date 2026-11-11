package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoInventarioEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeMedidaMaterial;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Almoxarifado central, requisição, transferência e inventário (ADR-0189). */
public final class CentralEstoqueDtos {

    private CentralEstoqueDtos() {
    }

    public record Central(UUID unidadeId, String nome) {
    }

    public record Requisicao(UUID uuid, UUID unidadeId, String unidadeNome, UUID centralId, String centralNome, PrioridadeRequisicaoEstoque prioridade,
                             SituacaoRequisicaoEstoque situacao, boolean parcial, String observacao, String solicitanteNome, Instant criadaEm,
                             Instant enviadaEm, Instant decididaEm, String atendenteNome, String motivo, UUID transferenciaId, Instant recebidaEm,
                             List<ItemRequisicao> itens) {
    }

    /** O item com o saldo e o consumo dos últimos 30 dias da unidade, e o saldo da central, para justificar e atender. */
    public record ItemRequisicao(UUID uuid, UUID materialId, String materialCodigo, String materialNome, UnidadeMedidaMaterial unidadeMedida,
                                 int quantidadePedida, Integer quantidadeAtendida, int saldoNaUnidade, int consumo30Dias, int saldoNaCentral) {
    }

    public record Transferencia(UUID uuid, UUID origemId, String origemNome, UUID destinoId, String destinoNome, UUID requisicaoId,
                                SituacaoTransferenciaEstoque situacao, String observacao, Instant enviadaEm, String enviadoPorNome, Instant recebidaEm,
                                String recebidoPorNome, boolean comDivergencia, List<ItemTransferencia> itens) {
    }

    public record ItemTransferencia(UUID uuid, UUID materialId, String materialCodigo, String materialNome, UUID loteOrigemId, String numeroLote,
                                    LocalDate validade, int quantidadeEnviada, Integer quantidadeRecebida, MotivoDivergenciaEstoque motivoDivergencia,
                                    String observacao) {
    }

    public record Inventario(UUID uuid, UUID unidadeId, String unidadeNome, SituacaoInventarioEstoque situacao, Instant abertoEm, String abertoPorNome,
                             Instant encerradoEm, String encerradoPorNome, String observacao, int lotes, int contados, int comDiferenca,
                             List<ItemInventario> itens) {
    }

    public record ItemInventario(UUID uuid, UUID loteId, UUID materialId, String materialCodigo, String materialNome, String numeroLote,
                                 LocalDate validade, int saldoSistema, Integer quantidadeContada, Integer diferenca, String contadoPorNome) {
    }
}
