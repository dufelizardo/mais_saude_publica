package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoEmergencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EventoEmergencialRepository extends JpaRepository<EventoEmergencial, UUID> {

    @Query("select e from EventoEmergencial e join fetch e.tipo left join fetch e.surto order by e.ocorridoEm desc")
    List<EventoEmergencial> findTodos();
}
