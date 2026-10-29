package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.Emergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EmergenciaRepository extends JpaRepository<Emergencia, UUID> {

    @Query("select e from Emergencia e join fetch e.tipo join fetch e.nivel join fetch e.evento left join fetch e.responsavel order by e.inicio desc")
    List<Emergencia> findTodas();

    java.util.Optional<Emergencia> findByEvento_Uuid(UUID eventoId);
}
