package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.ChecagemPrescricao;
import com.edufelizardo.maissaudepublica.models.HorarioPrescricao;
import com.edufelizardo.maissaudepublica.models.ItemPrescricao;
import com.edufelizardo.maissaudepublica.models.PrescricaoHospitalar;
import com.edufelizardo.maissaudepublica.models.enuns.AceitacaoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoChecagem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoHorarioPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Prescrição hospitalar, aprazamento e checagem (ADR-0187). */
public final class PrescricaoDtos {

    private PrescricaoDtos() {
    }

    public record Prescricao(UUID uuid, UUID internacaoId, UUID pacienteId, String pacienteNome, String leitoIdentificacao, String medicoMatricula,
                             String medicoNome, Instant inicio, Instant fim, Instant fimEfetivo, Instant encerradaEm, boolean vigente,
                             String observacao, UUID renovadaDe, Instant registradaEm, List<Item> itens) {

        public static Prescricao from(PrescricaoHospitalar p, Instant agora, List<Item> itens) {
            var i = p.getInternacao();
            return new Prescricao(p.getUuid(), i.getUuid(), i.getPaciente().getUuid(), i.getPaciente().getNome(), i.getLeito().getIdentificacao(),
                    p.getMedico().getMatricula(), p.getMedico().getNome(), p.getInicio(), p.getFim(), p.fimEfetivo(), p.getEncerradaEm(),
                    p.vigenteEm(agora), p.getObservacao(), p.getRenovadaDe(), p.getRegistradaEm(), itens);
        }
    }

    public record Item(UUID uuid, int ordem, TipoItemPrescricao tipo, UUID medicamentoId, String medicamentoNome, String dose, ViaAdministracao via,
                       TipoDieta tipoDieta, String descricao, Integer intervaloHoras, Instant primeiroHorario, boolean seNecessario, String condicao,
                       Instant suspensoEm, String motivoSuspensao, String suspensoPorNome, String resumo, List<Horario> horarios,
                       List<Checagem> checagens) {

        public static Item from(ItemPrescricao i, List<Horario> horarios, List<Checagem> checagens) {
            return new Item(i.getUuid(), i.getOrdem(), i.getTipo(), i.getMedicamento() != null ? i.getMedicamento().getUuid() : null,
                    i.getMedicamento() != null ? i.getMedicamento().getNome() : null, i.getDose(), i.getVia(), i.getTipoDieta(), i.getDescricao(),
                    i.getIntervaloHoras(), i.getPrimeiroHorario(), i.isSeNecessario(), i.getCondicao(), i.getSuspensoEm(), i.getMotivoSuspensao(),
                    i.getSuspensoPor() != null ? i.getSuspensoPor().getNome() : null, PrescricaoDtos.resumo(i), horarios, checagens);
        }
    }

    public record Horario(UUID uuid, UUID itemId, Instant previstoPara, SituacaoHorarioPrescricao situacao, boolean atrasado,
                          Instant aprazadoOriginalmente, String motivoReaprazamento, Checagem checagem) {

        public static Horario from(HorarioPrescricao h, boolean atrasado, Checagem checagem) {
            return new Horario(h.getUuid(), h.getItem().getUuid(), h.getPrevistoPara(), h.getSituacao(), atrasado, h.getAprazadoOriginalmente(),
                    h.getMotivoReaprazamento(), checagem);
        }
    }

    public record Checagem(UUID uuid, UUID itemId, UUID horarioId, ResultadoChecagem resultado, AceitacaoDieta aceitacao,
                           MotivoNaoAdministracao motivoNaoAdministracao, String observacao, UUID loteId, String loteNumero, Integer quantidade,
                           Instant realizadoEm, String profissionalNome, UUID retificacaoDeId, String motivoRetificacao, boolean retificada) {

        public static Checagem from(ChecagemPrescricao c) {
            return new Checagem(c.getUuid(), c.getItem().getUuid(), c.getHorario() != null ? c.getHorario().getUuid() : null, c.getResultado(),
                    c.getAceitacao(), c.getMotivoNaoAdministracao(), c.getObservacao(), c.getLote() != null ? c.getLote().getUuid() : null,
                    c.getLote() != null ? c.getLote().getNumeroLote() : null, c.getQuantidade(), c.getRealizadoEm(), c.getProfissional().getNome(),
                    c.getRetificacaoDe() != null ? c.getRetificacaoDe().getUuid() : null, c.getMotivoRetificacao(), c.isRetificada());
        }
    }

    /** Um horário no posto de enfermagem, com o paciente, o leito e o item. */
    public record HorarioDoPosto(UUID horarioId, UUID itemId, UUID prescricaoId, UUID internacaoId, String pacienteNome, String leitoIdentificacao,
                                 String setorNome, UUID unidadeId, String unidadeNome, TipoItemPrescricao tipo, String resumo, Instant previstoPara,
                                 SituacaoHorarioPrescricao situacao, boolean atrasado, Checagem checagem) {
    }

    /** O item em uma linha: "Dipirona 1 g IV de 6/6 h", "Dieta branda", "Sinais vitais de 4/4 h". */
    public static String resumo(ItemPrescricao i) {
        StringBuilder s = new StringBuilder();
        switch (i.getTipo()) {
            case MEDICAMENTO -> {
                s.append(i.getMedicamento() != null ? i.getMedicamento().getNome() : "Medicamento");
                if (i.getDose() != null) {
                    s.append(' ').append(i.getDose());
                }
                if (i.getVia() != null) {
                    s.append(' ').append(i.getVia().name().toLowerCase());
                }
            }
            case DIETA -> s.append("Dieta ").append(i.getTipoDieta() != null ? i.getTipoDieta().name().toLowerCase().replace('_', ' ') : "");
            case CUIDADO -> s.append(i.getDescricao() != null ? i.getDescricao() : "Cuidado");
        }
        if (i.isSeNecessario()) {
            s.append(" se necessário").append(i.getCondicao() != null ? " (" + i.getCondicao() + ")" : "");
        } else if (i.getIntervaloHoras() != null) {
            s.append(" de ").append(i.getIntervaloHoras()).append('/').append(i.getIntervaloHoras()).append(" h");
        }
        return s.toString();
    }
}
