package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoTripulante;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Respostas do Transporte (ADR-0178). */
public final class TransporteDtos {

    private TransporteDtos() {
    }

    public record Tripulante(UUID profissionalId, String nome, String matricula, FuncaoTripulante funcao) {
    }

    public record Plantao(UUID uuid, UUID veiculoId, String prefixo, TipoVeiculo tipo, Instant inicio, Instant fim, boolean vigente,
                          String observacao, List<Tripulante> tripulacao) {
    }

    public record Situacao(SituacaoVeiculo situacao, Instant inicio, String motivo) {
    }

    /** O veículo; no detalhe, o histórico de situações e os plantões dos últimos e próximos dias. */
    public record Veiculo(UUID uuid, String placa, String prefixo, TipoVeiculo tipo, boolean doSamu, String modelo, Integer ano, Integer lugares,
                          boolean maca, boolean cadeiraDeRodas, Integer quilometragem, UUID unidadeId, String unidadeNome, SituacaoVeiculo situacao,
                          Instant situacaoDesde, String observacao, Plantao plantaoVigente, List<Situacao> historico, List<Plantao> plantoes) {
    }

    /** Viatura pronta para o SAMU: disponível, com plantão vigente e sem ocorrência aberta. */
    public record ViaturaDisponivel(UUID veiculoId, String prefixo, TipoVeiculo tipo, String unidadeNome, Instant plantaoAte) {
    }
}
