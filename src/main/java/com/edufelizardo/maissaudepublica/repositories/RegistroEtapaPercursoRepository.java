package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RegistroEtapaPercurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroEtapaPercursoRepository extends JpaRepository<RegistroEtapaPercurso, UUID> {

    @Query("select r from RegistroEtapaPercurso r join fetch r.etapa where r.percurso.uuid in ?1 order by r.data")
    List<RegistroEtapaPercurso> findDosPercursos(Collection<UUID> percursoIds);
}
