package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    List<Agendamento> findByPaciente_UuidOrderByDataHoraAsc(UUID pacienteId);

    List<Agendamento> findByProfissional_UuidAndDataHoraBetweenOrderByDataHoraAsc(UUID profissionalId, java.time.LocalDateTime de,
                                                                                  java.time.LocalDateTime ate);
}
