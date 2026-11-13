package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.SolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Pedido de leito, eventos e vagas compatíveis da central de regulação de leitos (ADR-0184). */
public final class SolicitacaoLeitoDtos {

    private SolicitacaoLeitoDtos() {
    }

    public record Solicitacao(UUID uuid, UUID pacienteId, String pacienteNome, UUID unidadeOrigemId, String unidadeOrigemNome,
                              UUID internacaoOrigemId, UUID atendimentoId, UUID ocorrenciaUrgenciaId, String solicitadoPorMatricula,
                              String solicitadoPorNome, String cid, String quadroClinico, TipoLeito tipoLeito, PrioridadeRegulacao prioridade,
                              StatusSolicitacaoLeito status, Instant solicitadaEm, Integer posicaoNaFila, UUID leitoReservadoId,
                              String leitoReservadoIdentificacao, UUID unidadeDestinoId, String unidadeDestinoNome, Instant reservadoAte,
                              boolean vagaZero, String justificativaVagaZero, String transporte, String motivoEncerramento,
                              UUID internacaoDestinoId, List<Evento> eventos) {

        public static Solicitacao from(SolicitacaoLeito s, Integer posicao, List<EventoSolicitacaoLeito> eventos) {
            return new Solicitacao(s.getUuid(), s.getPaciente().getUuid(), s.getPaciente().getNome(), s.getUnidadeOrigem().getUuid(),
                    s.getUnidadeOrigem().getNome(), s.getInternacaoOrigem() != null ? s.getInternacaoOrigem().getUuid() : null,
                    s.getAtendimento() != null ? s.getAtendimento().getUuid() : null, s.getOcorrenciaUrgenciaId(),
                    s.getSolicitadoPor().getMatricula(), s.getSolicitadoPor().getNome(), s.getCid(), s.getQuadroClinico(), s.getTipoLeito(),
                    s.getPrioridade(), s.getStatus(), s.getSolicitadaEm(), posicao,
                    s.getLeitoReservado() != null ? s.getLeitoReservado().getUuid() : null,
                    s.getLeitoReservado() != null ? s.getLeitoReservado().getIdentificacao() : null,
                    s.getUnidadeDestino() != null ? s.getUnidadeDestino().getUuid() : null,
                    s.getUnidadeDestino() != null ? s.getUnidadeDestino().getNome() : null, s.getReservadoAte(), s.isVagaZero(),
                    s.getJustificativaVagaZero(), s.getTransporte(), s.getMotivoEncerramento(),
                    s.getInternacaoDestino() != null ? s.getInternacaoDestino().getUuid() : null,
                    eventos.stream().map(Evento::from).toList());
        }
    }

    public record Evento(TipoEventoSolicitacaoLeito tipo, String texto, String profissionalNome, Instant ocorridoEm) {
        static Evento from(EventoSolicitacaoLeito e) {
            return new Evento(e.getTipo(), e.getTexto(), e.getProfissional() != null ? e.getProfissional().getNome() : null, e.getOcorridoEm());
        }
    }

    /** Um leito livre de outra unidade que serve ao pedido: tipo, sexo da enfermaria e faixa etária. */
    public record Vaga(UUID leitoId, String identificacao, UUID unidadeId, String unidadeNome, String setorNome, TipoLeito tipo,
                       SexoLeito sexo, FaixaEtariaLeito faixaEtaria) {
        public static Vaga from(Leito l) {
            return new Vaga(l.getUuid(), l.getIdentificacao(), l.getUnidade().getUuid(), l.getUnidade().getNome(), l.getSetor().getNome(),
                    l.getTipo(), l.getSexo(), l.getFaixaEtaria());
        }
    }
}
