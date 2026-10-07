package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.TurnoEscala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TurnoEscalaRepository extends JpaRepository<TurnoEscala, UUID> {

    /** Turnos da unidade que começam no intervalo [de, ate). */
    @Query("select t from TurnoEscala t where t.unidade.uuid = :unidadeId and t.inicioEm >= :de and t.inicioEm < :ate order by t.inicioEm")
    List<TurnoEscala> daUnidadeEntre(UUID unidadeId, LocalDateTime de, LocalDateTime ate);

    /** Turnos dos profissionais (em qualquer unidade) que tocam o intervalo [de, ate). */
    @Query("select t from TurnoEscala t where t.profissional.uuid in :profissionais and t.inicioEm < :ate and t.fimEm > :de order by t.inicioEm")
    List<TurnoEscala> dosProfissionaisEntre(Collection<UUID> profissionais, LocalDateTime de, LocalDateTime ate);
}
