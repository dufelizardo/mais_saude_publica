package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EsquemaVacinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EsquemaVacinalRepository extends JpaRepository<EsquemaVacinal, UUID> {

    /** O calendário inteiro, na ordem de exibição (ADR-0147). */
    @Query("select e from EsquemaVacinal e join fetch e.imunobiologico order by e.ordem, e.dose")
    List<EsquemaVacinal> findTodos();
}
