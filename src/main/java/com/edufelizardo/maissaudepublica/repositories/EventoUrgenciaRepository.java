package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoUrgencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventoUrgenciaRepository extends JpaRepository<EventoUrgencia, UUID> {

    @Query("select e from EventoUrgencia e left join fetch e.profissional where e.ocorrencia.uuid = ?1 order by e.ocorridoEm, e.tipo")
    List<EventoUrgencia> findDaOcorrencia(UUID ocorrenciaId);

    @Query("select e from EventoUrgencia e where e.ocorrencia.uuid in ?1")
    List<EventoUrgencia> findDasOcorrencias(Collection<UUID> ocorrencias);
}
