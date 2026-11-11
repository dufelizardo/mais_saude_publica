package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmbarque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoViagem;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.TrechoTransporte;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Respostas do transporte eletivo e do TFD (ADR-0190). */
public final class TransporteEletivoDtos {

    private TransporteEletivoDtos() {
    }

    /** A solicitação; o laudo do TFD (CID e justificativa) só para quem pede ou autoriza. */
    public record Solicitacao(UUID uuid, UUID pacienteId, String pacienteNome, String pacienteCns, UUID unidadeId, String unidadeNome,
                              MotivoTransporte motivo, UUID agendamentoId, UUID solicitacaoRegulacaoId, UUID internacaoId, LocalDateTime dataHora,
                              String enderecoEmbarque, UUID destinoUnidadeId, String destino, String destinoMunicipio, TrechoTransporte trecho,
                              boolean maca, boolean cadeiraDeRodas, boolean oxigenio, String acompanhanteNome, String acompanhanteDocumento,
                              int lugares, String cid, String justificativa, String profissionalNome, SituacaoSolicitacaoTransporte situacao,
                              String motivoSituacao, UUID recorrenciaId, String observacao, Instant solicitadoEm, UUID viagemId) {
    }

    public record Embarque(UUID solicitacaoId, UUID pacienteId, String pacienteNome, MotivoTransporte motivo, LocalDateTime dataHora,
                           String enderecoEmbarque, String destino, TrechoTransporte trecho, boolean maca, boolean cadeiraDeRodas,
                           boolean oxigenio, String acompanhanteNome, SituacaoEmbarque situacao) {
    }

    public record Viagem(UUID uuid, UUID veiculoId, String prefixo, TipoVeiculo tipo, Integer lugaresVeiculo, UUID unidadeId, String unidadeNome,
                         UUID condutorId, String condutorNome, Instant saidaPrevista, Instant retornoPrevisto, String destino, SituacaoViagem situacao,
                         int lugaresOcupados, Integer kmSaida, Integer kmChegada, Integer kmPercorridos, Instant saidaEm, Instant chegadaEm,
                         String motivoCancelamento, String observacao, List<Embarque> embarques) {
    }

    /** A quilometragem rodada pelo veículo nas viagens concluídas do período. */
    public record Quilometragem(UUID veiculoId, String prefixo, TipoVeiculo tipo, String unidadeNome, int viagens, int pacientes, int km) {
    }
}
