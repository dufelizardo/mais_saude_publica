package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRedeTematica;
import com.edufelizardo.maissaudepublica.models.enuns.RedeTematica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Respostas das redes temáticas e da referência estrutural (GR2, ADR-0197). */
public final class RedeTematicaDtos {

    private RedeTematicaDtos() {
    }

    // ── Catálogo ─────────────────────────────────────────────────────────────────────────────────

    /** O perfil ou a capacidade ligada ao componente: {@code tipo} é PERFIL ou CAPACIDADE. */
    public record Item(UUID uuid, String tipo, UUID alvoId, String codigo, String nome) {
    }

    public record ComponenteDoCatalogo(ComponenteRedeTematica codigo, String nome, List<Item> itens) {
    }

    public record RedeDoCatalogo(RedeTematica codigo, String nome, List<ComponenteDoCatalogo> componentes) {
    }

    // ── Painel ───────────────────────────────────────────────────────────────────────────────────

    public record Ponto(UUID unidadeId, String unidadeNome, String unidadeTipo, List<String> itens) {
    }

    public record Componente(ComponenteRedeTematica codigo, String nome, List<Ponto> pontos) {
    }

    public record Painel(RedeTematica rede, String nome, String abrangencia, List<Componente> componentes, List<String> lacunas) {
    }

    // ── Referência ───────────────────────────────────────────────────────────────────────────────

    public record Referencia(UUID uuid, UUID origemId, String origemNome, String origemTipo, UUID procedimentoId, String procedimentoNome,
                             UUID executanteId, String executanteNome, LocalDate inicio, LocalDate fim, boolean vigente, String observacao) {
    }

    /** Nos últimos 90 dias: as autorizações em que havia referência e quantas foram para outra executante. */
    public record Indicador(UUID procedimentoId, String procedimentoNome, int autorizacoes, int foraDaReferencia, BigDecimal percentualFora) {
    }

    public record PainelReferencias(List<Referencia> referencias, List<Indicador> indicadores) {
    }

    /** A referência vigente que cobre a solicitante; os campos vêm nulos quando não há. */
    public record Sugestao(UUID referenciaId, UUID executanteId, String executanteNome, String origemNome) {
    }
}
