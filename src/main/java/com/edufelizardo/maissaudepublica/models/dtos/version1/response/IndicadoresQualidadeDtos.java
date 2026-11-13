package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.IndicadorQualidade;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** As respostas dos indicadores de qualidade e da auditoria de protocolos, fatia Q3 (ADR-0205). */
public final class IndicadoresQualidadeDtos {

    private IndicadoresQualidadeDtos() {
    }

    public record Ponto(String competencia, BigDecimal valor) {
    }

    /** situacao: DENTRO, FORA, SEM_META ou SEM_BASE (sem denominador para calcular). */
    public record Linha(IndicadorQualidade indicador, UUID checklistId, String nome, String medida, boolean menorMelhor, BigDecimal valor,
                        BigDecimal numerador, BigDecimal denominador, BigDecimal meta, String situacao, List<Ponto> serie) {
    }

    public record Painel(UUID unidadeId, String abrangencia, String competencia, List<Linha> linhas) {
    }

    public record Meta(UUID uuid, UUID unidadeId, String unidadeNome, IndicadorQualidade indicador, String indicadorNome, UUID checklistId,
                       String checklistNome, BigDecimal valor, LocalDate vigenteDesde, LocalDate vigenteAte) {
    }

    public record ItemChecklist(String codigo, String texto) {
    }

    public record Checklist(UUID uuid, String codigo, String nome, boolean ativo, UUID versaoId, int versao, List<ItemChecklist> itens) {
    }

    public record Auditoria(UUID uuid, UUID unidadeId, String unidadeNome, String setorNome, UUID checklistId, String checklistNome, int versao,
                            LocalDate dataAuditoria, int conformes, int aplicaveis, BigDecimal adesao, String observacao,
                            Map<String, RespostaRoteiro> respostas, Instant registradoEm) {
    }
}
