package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoAcompanhamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventoAcompanhamentoRepository extends JpaRepository<EventoAcompanhamento, UUID> {
    @Query("select e from EventoAcompanhamento e where e.acompanhamento.uuid in ?1 order by e.ocorridoEm")
    List<EventoAcompanhamento> findDosAcompanhamentos(Collection<UUID> acompanhamentoIds);
}
