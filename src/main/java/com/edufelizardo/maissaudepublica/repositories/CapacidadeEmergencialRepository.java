package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.CapacidadeEmergencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CapacidadeEmergencialRepository extends JpaRepository<CapacidadeEmergencial, UUID> {

    @Query("select c from CapacidadeEmergencial c join fetch c.unidade order by c.unidade.nome, c.recurso")
    List<CapacidadeEmergencial> findTodas();

    boolean existsByUnidade_UuidAndRecurso(UUID unidadeId, com.edufelizardo.maissaudepublica.models.enuns.RecursoCapacidade recurso);
}
