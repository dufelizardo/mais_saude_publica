package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.AcaoEmergencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AcaoEmergencialRepository extends JpaRepository<AcaoEmergencial, UUID> {

    @Query("select a from AcaoEmergencial a left join fetch a.unidade where a.emergencia.uuid = ?1 order by a.criadaEm")
    List<AcaoEmergencial> findDaEmergencia(UUID emergenciaId);
}
