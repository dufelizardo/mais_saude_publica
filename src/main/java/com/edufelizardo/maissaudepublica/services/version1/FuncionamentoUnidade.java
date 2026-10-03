package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.HorarioUnidade;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.HorarioUnidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quando a unidade atende (ADR-0101): fechada em obra ou inoperante; com horário estruturado (e sem funcionar 24 horas),
 * só dentro dos turnos do dia. Sem horário estruturado, a agenda segue como antes.
 */
@Component
public class FuncionamentoUnidade {

    @Autowired
    private HorarioUnidadeRepository horarioRepository;

    /** O funcionamento carregado uma vez, para a montagem da agenda. */
    public record Funcionamento(String nomeUnidade, boolean fechada, String situacao, boolean semRestricao,
                                Map<DayOfWeek, List<HorarioUnidade>> turnos) {

        /** O intervalo cabe inteiro num turno do dia (sem restrição de horário, sempre). Fechada, nunca. */
        public boolean aberta(LocalDateTime inicio, LocalDateTime fim) {
            if (fechada) {
                return false;
            }
            if (semRestricao) {
                return true;
            }
            if (!inicio.toLocalDate().equals(fim.toLocalDate()) && !fim.toLocalTime().equals(java.time.LocalTime.MIDNIGHT)) {
                return false;
            }
            return turnos.getOrDefault(inicio.getDayOfWeek(), List.of()).stream()
                    .anyMatch(t -> !inicio.toLocalTime().isBefore(t.getAbre()) && !fim.toLocalTime().isAfter(t.getFecha())
                            && fim.toLocalDate().equals(inicio.toLocalDate()));
        }
    }

    public Funcionamento de(UnidadeDeSaude unidade) {
        List<HorarioUnidade> turnos = horarioRepository.findByUnidade_UuidOrderByDiaSemanaAscAbreAsc(unidade.getUuid());
        boolean semRestricao = unidade.isFunciona24h() || turnos.isEmpty();
        return new Funcionamento(unidade.getNome(), unidade.getSituacaoOperacional().fechada(),
                unidade.getSituacaoOperacional().name(), semRestricao,
                turnos.stream().collect(Collectors.groupingBy(HorarioUnidade::getDiaSemana)));
    }
}
