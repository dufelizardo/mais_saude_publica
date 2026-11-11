package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoReservaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeMedidaMaterial;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** O estoque nas emergências (ES3, ADR-0191): a reserva, o consumo extraordinário e o resumo que a emergência mostra. */
public final class EstoqueEmergenciaDtos {

    private EstoqueEmergenciaDtos() {
    }

    public record Reserva(UUID uuid, UUID emergenciaId, String emergenciaNome, UUID unidadeId, String unidadeNome, UUID materialId, String materialCodigo,
                          String materialNome, UnidadeMedidaMaterial unidadeMedida, int quantidade, int consumida, int restante,
                          SituacaoReservaEstoque situacao, String justificativa, Instant criadaEm, Instant liberadaEm, String motivoLiberacao)
            implements Serializable {
    }

    /** O consumo da emergência por unidade e material, com o valor pelo custo dos lotes. */
    public record Consumo(UUID unidadeId, String unidadeNome, UUID materialId, String materialCodigo, String materialNome,
                          UnidadeMedidaMaterial unidadeMedida, int quantidade, BigDecimal valor) implements Serializable {
    }

    /** A emergência ativa ou em desmobilização, para o estoque escolher na reserva e no consumo. */
    public record EmergenciaAberta(UUID uuid, String nome, com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia situacao) implements Serializable {
    }

    public record Resumo(List<Reserva> reservas, List<Consumo> consumo, int quantidadeConsumida, BigDecimal valorConsumido) implements Serializable {
    }
}
