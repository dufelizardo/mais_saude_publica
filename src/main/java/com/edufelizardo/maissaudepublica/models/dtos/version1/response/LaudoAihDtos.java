package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.EventoLaudoAih;
import com.edufelizardo.maissaudepublica.models.FaixaAih;
import com.edufelizardo.maissaudepublica.models.LaudoAih;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.CausaExternaAih;
import com.edufelizardo.maissaudepublica.models.enuns.ClinicaAih;
import com.edufelizardo.maissaudepublica.models.enuns.StatusLaudoAih;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLaudoAih;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Laudo para solicitação de AIH, autorização interna e faixas de numeração (ADR-0186). */
public final class LaudoAihDtos {

    private LaudoAihDtos() {
    }

    public record Laudo(UUID uuid, UUID pacienteId, String pacienteNome, String pacienteCartaoSus, UUID unidadeId, String unidadeNome,
                        UUID internacaoId, UUID agendamentoId, CaraterInternacao carater, String solicitanteMatricula, String solicitanteNome,
                        String sinaisSintomas, String condicoesJustificativas, String resultadosExames, String diagnosticoInicial,
                        String cidPrincipal, String cidSecundario, String cidCausaAssociada, UUID procedimentoId, String procedimentoCodigo,
                        String procedimentoNome, String procedimentoSigtap, ClinicaAih clinica, CausaExternaAih causaExterna, StatusLaudoAih status,
                        Instant solicitadoEm, String numeroAih, String autorizadorNome, Instant decididoEm, String motivoDecisao,
                        List<Evento> eventos) {

        public static Laudo from(LaudoAih l, List<EventoLaudoAih> eventos) {
            return new Laudo(l.getUuid(), l.getPaciente().getUuid(), l.getPaciente().getNome(), l.getPaciente().getCartaoSus(), l.getUnidade().getUuid(),
                    l.getUnidade().getNome(), l.getInternacao() != null ? l.getInternacao().getUuid() : null,
                    l.getAgendamento() != null ? l.getAgendamento().getUuid() : null, l.getCarater(), l.getSolicitante().getMatricula(),
                    l.getSolicitante().getNome(), l.getSinaisSintomas(), l.getCondicoesJustificativas(), l.getResultadosExames(), l.getDiagnosticoInicial(),
                    l.getCidPrincipal(), l.getCidSecundario(), l.getCidCausaAssociada(), l.getProcedimento().getUuid(), l.getProcedimento().getCodigo(),
                    l.getProcedimento().getNome(), l.getProcedimento().getSigtap(), l.getClinica(), l.getCausaExterna(), l.getStatus(), l.getSolicitadoEm(),
                    l.getNumeroAih(), l.getAutorizador() != null ? l.getAutorizador().getNome() : null, l.getDecididoEm(), l.getMotivoDecisao(),
                    eventos.stream().map(Evento::from).toList());
        }
    }

    public record Evento(TipoEventoLaudoAih tipo, String texto, String profissionalNome, Instant ocorridoEm) {
        static Evento from(EventoLaudoAih e) {
            return new Evento(e.getTipo(), e.getTexto(), e.getProfissional() != null ? e.getProfissional().getNome() : null, e.getOcorridoEm());
        }
    }

    /** Faixa de numeração: os números já com o dígito verificador, para conferir com o documento do gestor. */
    public record Faixa(UUID uuid, int ano, long inicio, long fim, long proximo, long restantes, boolean ativa, boolean esgotada,
                        String primeiroNumero, String ultimoNumero, Instant registradaEm) {
        public static Faixa from(FaixaAih f, String primeiro, String ultimo) {
            return new Faixa(f.getUuid(), f.getAno(), f.getInicio(), f.getFim(), f.getProximo(), f.restantes(), f.isAtiva(), f.esgotada(), primeiro, ultimo,
                    f.getRegistradaEm());
        }
    }

    /** Internação de urgência sem laudo de AIH passadas 48 horas da admissão. */
    public record Pendencia(UUID internacaoId, UUID pacienteId, String pacienteNome, UUID unidadeId, String unidadeNome, String leitoIdentificacao,
                            Instant admitidaEm, long horasSemLaudo) {
    }
}
