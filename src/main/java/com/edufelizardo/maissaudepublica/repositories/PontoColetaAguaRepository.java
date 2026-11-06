package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.PontoColetaAgua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PontoColetaAguaRepository extends JpaRepository<PontoColetaAgua, UUID> {

    @Query("select p from PontoColetaAgua p left join fetch p.microarea m left join fetch m.area order by lower(p.nome)")
    List<PontoColetaAgua> findTodos();
}
