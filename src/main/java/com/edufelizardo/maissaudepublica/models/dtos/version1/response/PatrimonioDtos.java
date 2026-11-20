package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ClasseBem;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoBaixaBem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoBaixaBem;
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

    /**
     * {@code transferenciaPendente}: a transferência ainda sem aceite, se houver. {@code valorContabil}: o valor estimado pela depreciação
     * linear da classe (ADR-0217), só para leitura.
     */
    public record Bem(UUID uuid, String numeroTombamento, String descricao, ClasseBem classe, String marca, String modelo, String numeroSerie,
                      UUID unidadeId, String unidadeNome, UUID setorId, String setorNome, SituacaoBem situacao, OrigemBem origem,
                      BigDecimal valorAquisicao, LocalDate dataAquisicao, String documento, String fornecedorNome, LocalDate tombadoEm,
                      String responsavelMatricula, String responsavelNome, LocalDate responsavelDesde, UUID veiculoId, String veiculoPlaca,
                      Transferencia transferenciaPendente, String observacao, BigDecimal valorContabil) {
    }

    public record Responsavel(UUID uuid, String matricula, String nome, LocalDate desde, LocalDate ate) {
    }

    public record Transferencia(UUID uuid, UUID bemId, String numeroTombamento, String bemDescricao, UUID origemId, String origemNome, UUID destinoId,
                                String destinoNome, String motivo, LocalDate enviadaEm, SituacaoTransferenciaBem situacao, LocalDate resolvidaEm,
                                String motivoRecusa) {
    }

    /** A baixa do bem (ADR-0217). */
    public record Baixa(UUID uuid, UUID bemId, String numeroTombamento, String bemDescricao, String unidadeNome, MotivoBaixaBem motivo, String processo,
                        String boletimOcorrencia, LocalDate dataBaixa, String observacao, BigDecimal valorAquisicao, BigDecimal valorContabil,
                        SituacaoBaixaBem situacao, LocalDate resolvidaEm, String motivoRecusa) {
    }

    /** {@code baixa}: a mais recente, se houver. */
    public record Detalhe(Bem bem, List<Responsavel> responsaveis, List<Transferencia> transferencias, Baixa baixa) {
    }
}
