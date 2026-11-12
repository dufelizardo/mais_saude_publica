package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FatorContribuinte;
import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.TipoIncidente;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** As respostas da segurança do paciente, fatia Q1 (ADR-0203). */
public final class IncidenteSegurancaDtos {

    private IncidenteSegurancaDtos() {
    }

    /** O que o notificador recebe: só o protocolo (a análise é do núcleo). */
    public record Protocolo(UUID uuid, String numero, Instant registradoEm, boolean anonima) {
    }

    /** notivisaObrigatoria: evento adverso; prazoNotivisa: 72 horas no óbito e no never event, o 15º dia útil do mês seguinte nos demais. */
    public record Incidente(UUID uuid, String numero, UUID unidadeId, String unidadeNome, UUID pacienteId, String pacienteNome, Instant ocorridoEm,
                            String local, TipoIncidente tipo, GrauDano danoPercebido, String descricao, String acaoImediata, boolean anonima,
                            String notificadorNome, SituacaoIncidente situacao, NaturezaIncidente natureza, GrauDano dano, boolean neverEvent,
                            List<FatorContribuinte> fatores, String causaRaiz, String licoes, String motivoDescarte, Instant analisadoEm,
                            boolean exigeCausaRaiz, boolean notivisaObrigatoria, Instant prazoNotivisa, String numeroNotivisa, Instant notivisaEm,
                            boolean notivisaVencida, Instant registradoEm) {
    }

    public record Contagem(String codigo, long quantidade) {
    }

    public record Painel(long total, long aguardandoAnalise, long eventosAdversos, long notivisaPendentes, long notivisaVencidas,
                         List<Contagem> porTipo, List<Contagem> porDano, List<Contagem> porNatureza) {
    }
}
