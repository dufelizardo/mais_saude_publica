package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AdendoFichaAph;
import com.edufelizardo.maissaudepublica.models.FichaAph;
import com.edufelizardo.maissaudepublica.models.MedicamentoAph;
import com.edufelizardo.maissaudepublica.models.OcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.SinaisVitaisAph;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAph;
import com.edufelizardo.maissaudepublica.models.enuns.MecanismoTrauma;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaAph;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.ProcedimentoAph;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRecursoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Ficha clínica do atendimento pré-hospitalar (ADR-0188). */
public final class FichaAphDtos {

    private FichaAphDtos() {
    }

    /** A ocorrência na lista da equipe, com a situação da ficha. */
    public record Atendimento(UUID ocorrenciaId, String protocolo, UUID centralId, String centralNome, SituacaoOcorrenciaUrgencia situacao,
                              PrioridadeRegulacao prioridade, String pacienteNome, String queixa, String endereco, TipoRecursoUrgencia tipoRecurso,
                              String recurso, String destinoNome, Instant recebidaEm, UUID fichaId, boolean fichaFinalizada) {

        public static Atendimento from(OcorrenciaUrgencia o, FichaAph f) {
            return new Atendimento(o.getUuid(), o.getProtocolo(), o.getCentral().getUuid(), o.getCentral().getNome(), o.getSituacao(), o.getPrioridade(),
                    FichaAphDtos.paciente(o), o.getQueixa(), o.getEndereco(), o.getTipoRecurso(), FichaAphDtos.recurso(o), o.getDestino() != null ? o.getDestino().getNome() : null,
                    o.getRecebidaEm(), f != null ? f.getUuid() : null, f != null && f.finalizada());
        }
    }

    public record Ficha(UUID uuid, UUID ocorrenciaId, String protocolo, String centralNome, String pacienteNome, String recurso, String destinoNome,
                        NaturezaAph natureza, MecanismoTrauma mecanismoTrauma, String situacaoEncontrada, Boolean hemorragiaExsanguinante,
                        String viaAerea, String respiracao, String circulacao, Integer glasgowOcular, Integer glasgowVerbal, Integer glasgowMotor,
                        Integer glasgowTotal, String pupilas, String exposicao, List<ProcedimentoAph> procedimentos, String procedimentoOutro,
                        String impressaoDiagnostica, String cid, DesfechoAph desfecho, String recusaNome, Instant obitoEm,
                        String obitoConstatadoPorNome, String recebidoPor, Instant entregueEm, Instant abertaEm, Instant atualizadaEm,
                        Instant finalizadaEm, String finalizadaPorNome, boolean finalizada, List<Sinais> sinaisVitais, List<Medicamento> medicamentos,
                        List<Adendo> adendos) {

        public static Ficha from(FichaAph f, List<SinaisVitaisAph> sinais, List<MedicamentoAph> medicamentos, List<AdendoFichaAph> adendos) {
            OcorrenciaUrgencia o = f.getOcorrencia();
            return new Ficha(f.getUuid(), o.getUuid(), o.getProtocolo(), o.getCentral().getNome(), FichaAphDtos.paciente(o), FichaAphDtos.recurso(o),
                    o.getDestino() != null ? o.getDestino().getNome() : null, f.getNatureza(), f.getMecanismoTrauma(), f.getSituacaoEncontrada(),
                    f.getHemorragiaExsanguinante(), f.getViaAerea(), f.getRespiracao(), f.getCirculacao(), f.getGlasgowOcular(), f.getGlasgowVerbal(),
                    f.getGlasgowMotor(), f.glasgowTotal(), f.getPupilas(), f.getExposicao(), f.getProcedimentos().stream().sorted().toList(),
                    f.getProcedimentoOutro(), f.getImpressaoDiagnostica(), f.getCid(), f.getDesfecho(), f.getRecusaNome(), f.getObitoEm(),
                    f.getObitoConstatadoPor() != null ? f.getObitoConstatadoPor().getNome() : null, f.getRecebidoPor(), f.getEntregueEm(),
                    f.getAbertaEm(), f.getAtualizadaEm(), f.getFinalizadaEm(), f.getFinalizadaPor() != null ? f.getFinalizadaPor().getNome() : null,
                    f.finalizada(), sinais.stream().map(Sinais::from).toList(), medicamentos.stream().map(Medicamento::from).toList(),
                    adendos.stream().map(Adendo::from).toList());
        }
    }

    public record Sinais(UUID uuid, Instant medidoEm, Integer paSistolica, Integer paDiastolica, Integer frequenciaCardiaca,
                         Integer frequenciaRespiratoria, Integer saturacao, BigDecimal temperatura, Integer glicemia, Integer dor, Integer glasgow,
                         String profissionalNome) {
        static Sinais from(SinaisVitaisAph s) {
            return new Sinais(s.getUuid(), s.getMedidoEm(), s.getPaSistolica(), s.getPaDiastolica(), s.getFrequenciaCardiaca(),
                    s.getFrequenciaRespiratoria(), s.getSaturacao(), s.getTemperatura(), s.getGlicemia(), s.getDor(), s.getGlasgow(),
                    s.getProfissional().getNome());
        }
    }

    public record Medicamento(UUID uuid, String nome, String dose, ViaAdministracao via, Instant administradoEm, String profissionalNome) {
        static Medicamento from(MedicamentoAph m) {
            return new Medicamento(m.getUuid(), m.getNome(), m.getDose(), m.getVia(), m.getAdministradoEm(), m.getProfissional().getNome());
        }
    }

    public record Adendo(UUID uuid, String texto, String profissionalNome, Instant registradoEm) {
        static Adendo from(AdendoFichaAph a) {
            return new Adendo(a.getUuid(), a.getTexto(), a.getProfissional().getNome(), a.getRegistradoEm());
        }
    }

    static String paciente(OcorrenciaUrgencia o) {
        return o.getPaciente() != null ? o.getPaciente().getNome() : o.getDescricaoPaciente() != null ? o.getDescricaoPaciente() : "Sem identificação";
    }

    static String recurso(OcorrenciaUrgencia o) {
        String id = o.getVeiculo() != null ? o.getVeiculo().getPrefixo() : o.getRecursoIdentificacao();
        return (o.getTipoRecurso() != null ? o.getTipoRecurso().name() : "") + (id != null ? " " + id : "");
    }
}
