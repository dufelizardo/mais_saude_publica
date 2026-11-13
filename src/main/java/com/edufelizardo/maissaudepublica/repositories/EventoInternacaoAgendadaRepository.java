package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoInternacaoAgendada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoInternacaoAgendadaRepository extends JpaRepository<EventoInternacaoAgendada, UUID> {
    List<EventoInternacaoAgendada> findByAgendamento_UuidOrderByOcorridoEmAsc(UUID agendamentoId);
}
