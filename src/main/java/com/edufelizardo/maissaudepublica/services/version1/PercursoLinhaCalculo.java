package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.EtapaLinhaCuidado;
import com.edufelizardo.maissaudepublica.models.RegistroEtapaPercurso;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PercursoLinhaDto;
import com.edufelizardo.maissaudepublica.models.enuns.ReferenciaPrazoEtapa;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEtapaPercurso;

import java.time.LocalDate;
import java.util.*;

/**
 * A situação das etapas do percurso (ADR-0152), sem banco, como a situação vacinal: para cada etapa ativa, na ordem, a
 * referência do prazo (o início, ou o cumprimento da anterior), as evidências desde a referência e a quantidade pedida.
 * Cumprida quando chega à quantidade; dispensada quando há a dispensa; senão atrasada (prazo vencido), no prazo ou
 * aguardando a anterior (a referência ainda não existe).
 */
final class PercursoLinhaCalculo {

    /** Algo registrado que cumpre (ou anda) a etapa: a data e o texto. */
    record Evidencia(LocalDate data, String descricao) {
    }

    private PercursoLinhaCalculo() {
    }

    static List<PercursoLinhaDto.Etapa> calcular(List<EtapaLinhaCuidado> etapas, LocalDate inicio, Map<UUID, List<Evidencia>> evidencias,
                                                 Map<UUID, String> andamentos, Map<UUID, RegistroEtapaPercurso> dispensas, LocalDate hoje) {
        List<PercursoLinhaDto.Etapa> lista = new ArrayList<>();
        LocalDate anterior = inicio;
        boolean primeira = true;
        for (EtapaLinhaCuidado e : etapas) {
            LocalDate base = primeira || e.getPrazoDesde() == ReferenciaPrazoEtapa.INICIO ? inicio : anterior;
            primeira = false;
            List<Evidencia> todas = evidencias.getOrDefault(e.getUuid(), List.of());
            RegistroEtapaPercurso dispensa = dispensas.get(e.getUuid());
            if (base == null) {
                lista.add(etapa(e, 0, SituacaoEtapaPercurso.AGUARDANDO, null, null, andamentos.get(e.getUuid()), dispensa));
                anterior = null;
                continue;
            }
            List<Evidencia> validas = todas.stream().filter(x -> !x.data().isBefore(base)).sorted(Comparator.comparing(Evidencia::data)).toList();
            int quantidade = Math.max(1, e.getQuantidade());
            if (validas.size() >= quantidade) {
                Evidencia cumpriu = validas.get(quantidade - 1);
                lista.add(etapa(e, validas.size(), SituacaoEtapaPercurso.CUMPRIDA, null, cumpriu.data(), cumpriu.descricao(), null));
                anterior = cumpriu.data();
                continue;
            }
            LocalDate prazo = e.getPrazoDias() == null ? null : base.plusDays(e.getPrazoDias());
            if (dispensa != null) {
                lista.add(etapa(e, validas.size(), SituacaoEtapaPercurso.DISPENSADA, prazo, null, null, dispensa));
                anterior = dispensa.getData();
                continue;
            }
            SituacaoEtapaPercurso s = prazo != null && hoje.isAfter(prazo) ? SituacaoEtapaPercurso.ATRASADA : SituacaoEtapaPercurso.PENDENTE;
            String andamento = andamentos.get(e.getUuid());
            if (andamento == null && !validas.isEmpty()) {
                andamento = validas.size() + " de " + quantidade + " · a última " + validas.get(validas.size() - 1).descricao();
            }
            lista.add(etapa(e, validas.size(), s, prazo, null, andamento, null));
            anterior = null;
        }
        return lista;
    }

    private static PercursoLinhaDto.Etapa etapa(EtapaLinhaCuidado e, int realizadas, SituacaoEtapaPercurso s, LocalDate prazo, LocalDate cumpridaEm,
                                                String evidencia, RegistroEtapaPercurso dispensa) {
        return new PercursoLinhaDto.Etapa(e.getUuid(), e.getOrdem(), e.getNome(), e.getTipo(), e.isObrigatoria(), Math.max(1, e.getQuantidade()),
                realizadas, s, prazo, cumpridaEm, evidencia, dispensa != null ? dispensa.getObservacao() : null);
    }
}
