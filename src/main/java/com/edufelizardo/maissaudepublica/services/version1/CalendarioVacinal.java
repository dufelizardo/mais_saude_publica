package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.EsquemaVacinal;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DoseCalendarioDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDoseCalendario;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * A situação vacinal pelo calendário (ADR-0147), sem banco: para cada dose que conta na situação, se foi aplicada, está
 * atrasada, vem nos próximos 30 dias, é futura ou passou da idade máxima. A data prevista sai da idade recomendada e,
 * quando há, do intervalo mínimo desde a dose anterior do mesmo imunobiológico; a dose que se repete (o dT a cada 10
 * anos) vale pela última aplicada.
 */
final class CalendarioVacinal {

    static final int JANELA_PROXIMAS_DIAS = 30;

    private CalendarioVacinal() {
    }

    static long idadeMeses(LocalDate nascimento, LocalDate dia) {
        return Period.between(nascimento, dia).toTotalMonths();
    }

    static List<DoseCalendarioDto> situacao(LocalDate nascimento, LocalDate hoje, List<EsquemaVacinal> esquemas, List<RegistroVacinacao> doses) {
        Map<UUID, List<RegistroVacinacao>> porImuno = new HashMap<>();
        for (RegistroVacinacao r : doses) {
            porImuno.computeIfAbsent(r.getImunobiologico().getUuid(), k -> new ArrayList<>()).add(r);
        }
        porImuno.values().forEach(l -> l.sort(Comparator.comparing(RegistroVacinacao::getAplicadaEm)));
        List<EsquemaVacinal> conta = esquemas.stream().filter(e -> e.isContaNaSituacao() && e.vigenteEm(hoje))
                .sorted(Comparator.comparingInt(EsquemaVacinal::getOrdem)).toList();
        List<DoseCalendarioDto> lista = new ArrayList<>();
        Map<UUID, LocalDate> anterior = new HashMap<>();
        for (EsquemaVacinal e : conta) {
            UUID imuno = e.getImunobiologico().getUuid();
            List<RegistroVacinacao> doImuno = porImuno.getOrDefault(imuno, List.of());
            LocalDate porIdade = nascimento.plusMonths(e.getIdadeRecomendadaMeses() != null ? e.getIdadeRecomendadaMeses()
                    : e.getIdadeMinimaMeses() != null ? e.getIdadeMinimaMeses() : 0);
            if (e.getRepeticaoAnos() != null) {
                RegistroVacinacao ultima = doImuno.isEmpty() ? null : doImuno.get(doImuno.size() - 1);
                LocalDate prevista = ultima != null ? ultima.getAplicadaEm().toLocalDate().plusYears(e.getRepeticaoAnos()) : porIdade;
                if (prevista.isBefore(porIdade)) {
                    prevista = porIdade;
                }
                SituacaoDoseCalendario s = ultima != null && prevista.isAfter(hoje) ? SituacaoDoseCalendario.APLICADA : classificar(prevista, hoje);
                lista.add(dto(e, prevista, s, s == SituacaoDoseCalendario.APLICADA ? ultima : null));
                continue;
            }
            RegistroVacinacao feita = doImuno.stream().filter(r -> r.getDose() == e.getDose()).findFirst().orElse(null);
            LocalDate prevista = porIdade;
            LocalDate dataAnterior = anterior.get(imuno);
            if (e.getIntervaloMinimoDias() != null && dataAnterior != null && dataAnterior.plusDays(e.getIntervaloMinimoDias()).isAfter(prevista)) {
                prevista = dataAnterior.plusDays(e.getIntervaloMinimoDias());
            }
            SituacaoDoseCalendario s;
            if (feita != null) {
                s = SituacaoDoseCalendario.APLICADA;
                anterior.put(imuno, feita.getAplicadaEm().toLocalDate());
            } else if (e.getIdadeMaximaMeses() != null && idadeMeses(nascimento, hoje) > e.getIdadeMaximaMeses()) {
                s = SituacaoDoseCalendario.FORA_DA_IDADE;
            } else {
                s = classificar(prevista, hoje);
            }
            lista.add(dto(e, prevista, s, feita));
        }
        return lista;
    }

    private static SituacaoDoseCalendario classificar(LocalDate prevista, LocalDate hoje) {
        if (!prevista.isAfter(hoje)) {
            return SituacaoDoseCalendario.ATRASADA;
        }
        return ChronoUnit.DAYS.between(hoje, prevista) <= JANELA_PROXIMAS_DIAS ? SituacaoDoseCalendario.PROXIMA : SituacaoDoseCalendario.FUTURA;
    }

    private static DoseCalendarioDto dto(EsquemaVacinal e, LocalDate prevista, SituacaoDoseCalendario s, RegistroVacinacao r) {
        return new DoseCalendarioDto(e.getUuid(), e.getImunobiologico().getUuid(), e.getImunobiologico().getNome(), e.getImunobiologico().getSigla(),
                e.getDose(), e.getDose().rotulo(), e.getPublico(), e.getObservacao(), prevista, s, r != null ? r.getUuid() : null,
                r != null ? r.getAplicadaEm() : null);
    }
}
