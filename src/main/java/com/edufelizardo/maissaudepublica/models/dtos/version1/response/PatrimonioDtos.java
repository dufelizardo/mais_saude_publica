package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ClasseBem;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemBem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoBem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaBem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas do Patrimônio, fatia PM1 (ADR-0215). */
public final class PatrimonioDtos {

    private PatrimonioDtos() {
    }

    /** {@code transferenciaPendente}: a transferência ainda sem aceite, se houver. */
    public record Bem(UUID uuid, String numeroTombamento, String descricao, ClasseBem classe, String marca, String modelo, String numeroSerie,
                      UUID unidadeId, String unidadeNome, UUID setorId, String setorNome, SituacaoBem situacao, OrigemBem origem,
                      BigDecimal valorAquisicao, LocalDate dataAquisicao, String documento, String fornecedorNome, LocalDate tombadoEm,
                      String responsavelMatricula, String responsavelNome, LocalDate responsavelDesde, UUID veiculoId, String veiculoPlaca,
                      Transferencia transferenciaPendente, String observacao) {
    }

    public record Responsavel(UUID uuid, String matricula, String nome, LocalDate desde, LocalDate ate) {
    }

    public record Transferencia(UUID uuid, UUID bemId, String numeroTombamento, String bemDescricao, UUID origemId, String origemNome, UUID destinoId,
                                String destinoNome, String motivo, LocalDate enviadaEm, SituacaoTransferenciaBem situacao, LocalDate resolvidaEm,
                                String motivoRecusa) {
    }

    public record Detalhe(Bem bem, List<Responsavel> responsaveis, List<Transferencia> transferencias) {
    }
}
