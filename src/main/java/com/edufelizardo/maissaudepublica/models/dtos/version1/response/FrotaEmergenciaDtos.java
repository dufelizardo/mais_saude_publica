package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Os veículos nas emergências (T3, ADR-0192): o pedido da coordenação, a cessão pela frota e o que a emergência vê da frota. */
public final class FrotaEmergenciaDtos {

    private FrotaEmergenciaDtos() {
    }

    public record Cessao(UUID uuid, UUID mobilizacaoId, UUID emergenciaId, String emergenciaNome, UUID veiculoId, String prefixo, String placa,
                         TipoVeiculo tipo, String unidadeNome, Instant cedidaEm, Instant devolvidaEm, Integer kmInicio, Integer kmFim,
                         String observacao) implements Serializable {
    }

    /** O pedido de veículo da emergência ativa, com o que já foi cedido; {@code faltam} é nulo quando a quantidade não foi dita. */
    public record Pedido(UUID mobilizacaoId, UUID emergenciaId, String emergenciaNome, UUID destinoId, String destinoNome, String recurso,
                         Integer quantidade, String responsavel, LocalDate inicio, LocalDate fim, SituacaoMobilizacao situacao, List<Cessao> cessoes,
                         Integer faltam) implements Serializable {
    }

    /** A cessão feita e os avisos: as viagens eletivas programadas do veículo que a frota precisa reprogramar. */
    public record CessaoFeita(Cessao cessao, List<String> avisos) implements Serializable {
    }

    public record Veiculo(UUID veiculoId, String prefixo, TipoVeiculo tipo, String unidadeNome, SituacaoVeiculo situacao, String cedidoA)
            implements Serializable {
    }

    public record Disponiveis(TipoVeiculo tipo, int quantidade) implements Serializable {
    }

    /** O que o detalhe da emergência mostra: os cedidos a ela, a frota das unidades afetadas e os disponíveis da rede por tipo. */
    public record Frota(List<Cessao> cedidos, List<Veiculo> unidadesAfetadas, List<Disponiveis> disponiveisPorTipo) implements Serializable {
    }
}
