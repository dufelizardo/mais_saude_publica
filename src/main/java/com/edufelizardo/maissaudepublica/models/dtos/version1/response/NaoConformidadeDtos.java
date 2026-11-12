package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaCausa;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoQualidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNaoConformidade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** As respostas das não conformidades e dos planos de ação, fatia Q2 (ADR-0204). */
public final class NaoConformidadeDtos {

    private NaoConformidadeDtos() {
    }

    public record Acao(UUID uuid, int ciclo, String oQue, String porQue, String responsavelMatricula, String responsavelNome, String onde,
                       LocalDate prazo, String como, BigDecimal custo, SituacaoAcaoQualidade situacao, boolean atrasada, String evidencia,
                       LocalDate concluidaEm, String motivoCancelamento) {
    }

    public record Verificacao(int ciclo, boolean eficaz, LocalDate verificadaEm, String observacao) {
    }

    public record NaoConformidade(UUID uuid, String numero, UUID unidadeId, String unidadeNome, UUID setorId, String setorNome,
                                  OrigemNaoConformidade origem, UUID incidenteId, String incidenteNumero, UUID irregularidadeId,
                                  String irregularidadeDescricao, String descricao, String correcaoImediata, GravidadeNaoConformidade gravidade,
                                  LocalDate identificadaEm, String responsavelMatricula, String responsavelNome, String analiseCausa,
                                  List<CategoriaCausa> categoriasCausa, SituacaoNaoConformidade situacao, int ciclo, LocalDate verificacaoPrevista,
                                  LocalDate encerradaEm, String motivoCancelamento, long acoesAtrasadas, List<Acao> acoes,
                                  List<Verificacao> verificacoes, Instant registradoEm) {
    }

    public record MinhaAcao(UUID naoConformidadeId, String numero, String unidadeNome, String naoConformidadeDescricao, Acao acao) {
    }

    /** origem: INCIDENTE ou INSPECAO_SANITARIA; referenciaId: o incidente ou a irregularidade. */
    public record Sugestao(OrigemNaoConformidade origem, UUID referenciaId, String referencia, UUID unidadeId, String unidadeNome, String descricao,
                           LocalDate data) {
    }

    public record Contagem(String codigo, long quantidade) {
    }

    /** eficaciaNaPrimeira: das não conformidades já verificadas, o percentual eficaz na primeira verificação. */
    public record Painel(long total, long emAberto, long acoesAtrasadas, long verificadas, Double eficaciaNaPrimeira, List<Contagem> porOrigem,
                         List<Contagem> porGravidade, List<Contagem> porSituacao) {
    }
}
