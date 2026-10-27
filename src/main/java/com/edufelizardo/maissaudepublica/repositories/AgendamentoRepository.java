package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    List<Agendamento> findByPaciente_UuidOrderByDataHoraAsc(UUID pacienteId);

    List<Agendamento> findByProfissional_UuidAndDataHoraBetweenOrderByDataHoraAsc(UUID profissionalId, java.time.LocalDateTime de,
                                                                                  java.time.LocalDateTime ate);

    /** Produção assistencial (ADR-0143): [unidade, dataHora, status, encaixe] de cada marcação no intervalo. */
    @org.springframework.data.jpa.repository.Query("select g.unidade.uuid, g.dataHora, g.status, g.encaixe from Agendamento g "
            + "where g.unidade is not null and g.dataHora >= ?1 and g.dataHora < ?2")
    List<Object[]> findParaProducao(java.time.LocalDateTime de, java.time.LocalDateTime ate);
}
