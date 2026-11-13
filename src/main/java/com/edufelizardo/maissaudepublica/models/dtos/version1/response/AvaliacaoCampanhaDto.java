package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoAtingido;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMetaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A avaliação da campanha de comunicação (C6, ADR-0196): a apuração ao vivo (canais, ações educativas, cobertura por território, metas e
 * efetividade) e, na encerrada, a avaliação registrada com os números congelados.
 */
public record AvaliacaoCampanhaDto(UUID campanhaId, String campanhaNome, SituacaoCampanhaComunicacao situacao, LocalDate inicio, LocalDate fim,
                                   List<Canal> canais, int enviados, Acoes acoes, List<Territorio> territorios, SemTerritorio semTerritorio,
                                   BigDecimal cobertura, Meta metaPessoas, Meta metaAcoes, Efetividade efetividade, Registrada avaliacao) {

    /** Por canal; o Portal não conta pessoas. O destaque é o canal que mais entregou. */
    public record Canal(String nome, TipoCanalComunicacao tipo, int publicacoes, int enviados, int falhas, BigDecimal taxaEntrega,
                        boolean semContagem, boolean destaque) {
    }

    /** As ações educativas ligadas; pessoas, participação e materiais contam só as realizadas. */
    public record Acoes(int previstas, int realizadas, int naoRealizadas, int canceladas, int pessoas, int participacaoAlta,
                        int participacaoMedia, int participacaoBaixa, int semAvaliacao, List<Material> materiais) {
    }

    public record Material(String titulo, int quantidade) {
    }

    /** A cobertura: microáreas com ação realizada sobre as microáreas do território. */
    public record Territorio(UUID uuid, String nome, int microareas, int microareasComAcao, BigDecimal cobertura, long moradores, int pessoas,
                             boolean menor) {
    }

    public record SemTerritorio(int acoes, int pessoas) {
    }

    public record Meta(Integer meta, int valor, BigDecimal percentual, SituacaoMetaComunicacao situacao) {
    }

    /** Indicadores de resultado associados à campanha; não provam causa. */
    public record Efetividade(String campanhaOperacional, Integer metaOperacional, Integer realizadoOperacional, BigDecimal percentualOperacional,
                              String programa, LocalDate de, LocalDate ate, Long novosNoPeriodo, LocalDate anteriorDe, LocalDate anteriorAte,
                              Long novosNoPeriodoAnterior) {
    }

    /** O julgamento da equipe, com os números congelados no registro. */
    public record Registrada(PublicoAtingido publicoAtingido, String resultados, String conclusoes, String recomendacoes, int pessoasAlcancadas,
                             int acoesRealizadas, int enviados, BigDecimal cobertura, SituacaoMetaComunicacao situacaoMetaPessoas,
                             SituacaoMetaComunicacao situacaoMetaAcoes, Instant registradaEm, Instant atualizadaEm) {
    }

    // ── Para os relatórios (#19) ─────────────────────────────────────────────────────────────────────

    public record NoPeriodo(LocalDate de, LocalDate ate, String abrangencia, List<AvaliacaoCampanhaDto> campanhas) {
    }

    public record EducacaoNoPeriodo(LocalDate de, LocalDate ate, String abrangencia, int acoes, int pessoas, List<Tema> temas,
                                    List<Unidade> unidades, List<Material> materiais) {
    }

    public record Tema(String tema, int acoes, int pessoas, int alta, int media, int baixa, int semAvaliacao) {
    }

    public record Unidade(String nome, int acoes, int pessoas) {
    }
}
