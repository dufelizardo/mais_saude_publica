package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.CensoDiario;
import com.edufelizardo.maissaudepublica.models.CensoDiarioPaciente;
import com.edufelizardo.maissaudepublica.models.EventoInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.InternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Internação eletiva agendada e censo hospitalar diário (ADR-0185). */
public final class EletivaCensoDtos {

    private EletivaCensoDtos() {
    }

    public record Agendamento(UUID uuid, UUID pacienteId, String pacienteNome, UUID unidadeId, String unidadeNome, LocalDate dataPrevista,
                              TipoLeito tipoLeito, String medicoMatricula, String medicoNome, String cid, String procedimento, Integer previsaoDias,
                              UUID solicitacaoRegulacaoId, StatusInternacaoAgendada status, UUID leitoReservadoId, String leitoReservadoIdentificacao,
                              Instant reservadoAte, UUID internacaoId, String motivoEncerramento, boolean aguardandoDesfecho, boolean podeReservar,
                              List<Evento> eventos) {

        public static Agendamento from(InternacaoAgendada a, StatusInternacaoAgendada status, boolean aguardandoDesfecho, boolean podeReservar,
                                       List<EventoInternacaoAgendada> eventos) {
            boolean reservada = status == StatusInternacaoAgendada.RESERVADA;
            return new Agendamento(a.getUuid(), a.getPaciente().getUuid(), a.getPaciente().getNome(), a.getUnidade().getUuid(), a.getUnidade().getNome(),
                    a.getDataPrevista(), a.getTipoLeito(), a.getMedico().getMatricula(), a.getMedico().getNome(), a.getCid(), a.getProcedimento(),
                    a.getPrevisaoDias(), a.getSolicitacaoRegulacaoId(), status,
                    reservada && a.getLeitoReservado() != null ? a.getLeitoReservado().getUuid() : null,
                    reservada && a.getLeitoReservado() != null ? a.getLeitoReservado().getIdentificacao() : null,
                    reservada ? a.getReservadoAte() : null, a.getInternacao() != null ? a.getInternacao().getUuid() : null,
                    a.getMotivoEncerramento(), aguardandoDesfecho, podeReservar, eventos.stream().map(Evento::from).toList());
        }
    }

    public record Evento(TipoEventoInternacaoAgendada tipo, String texto, String profissionalNome, Instant ocorridoEm) {
        static Evento from(EventoInternacaoAgendada e) {
            return new Evento(e.getTipo(), e.getTexto(), e.getProfissional() != null ? e.getProfissional().getNome() : null, e.getOcorridoEm());
        }
    }

    /** O censo do dia: fechado (com uuid e versão) ou a prévia calculada (sem uuid). */
    public record Censo(UUID uuid, UUID unidadeId, String unidadeNome, LocalDate data, Integer versao, boolean fechado, int leitosExistentes,
                        int leitosBloqueados, int leitosOperacionais, int internadosInicio, int admissoes, int altasMelhorado, int altasAPedido,
                        int altasTransferencia, int evasoes, int obitos, int transferenciasInternas, int internadosFim, int pacienteDia,
                        BigDecimal taxaOcupacao, BigDecimal mediaPermanencia, Instant fechadoEm, boolean reaberto, String motivoReabertura,
                        List<PacienteDoCenso> pacientes) {

        public static Censo from(CensoDiario c, List<CensoDiarioPaciente> pacientes) {
            return new Censo(c.getUuid(), c.getUnidade().getUuid(), c.getUnidade().getNome(), c.getData(), c.getVersao(), true, c.getLeitosExistentes(),
                    c.getLeitosBloqueados(), c.getLeitosOperacionais(), c.getInternadosInicio(), c.getAdmissoes(), c.getAltasMelhorado(),
                    c.getAltasAPedido(), c.getAltasTransferencia(), c.getEvasoes(), c.getObitos(), c.getTransferenciasInternas(), c.getInternadosFim(),
                    c.getPacienteDia(), c.getTaxaOcupacao(), c.getMediaPermanencia(), c.getFechadoEm(), c.isReaberto(), c.getMotivoReabertura(),
                    pacientes.stream().map(p -> new PacienteDoCenso(p.getInternacao().getUuid(), p.getPacienteNome(), p.getLeitoIdentificacao(),
                            p.getSetorNome(), p.getDiasInternado())).toList());
        }
    }

    public record PacienteDoCenso(UUID internacaoId, String pacienteNome, String leitoIdentificacao, String setorNome, int diasInternado) {
    }

    /** Um dia terminado sem censo fechado. */
    public record Pendencia(UUID unidadeId, String unidadeNome, LocalDate data) {
    }
}
