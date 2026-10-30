package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.ExposicaoAmbiental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExposicaoAmbientalRepository extends JpaRepository<ExposicaoAmbiental, UUID> {

    @Query("select x from ExposicaoAmbiental x where x.emergencia.uuid = ?1 order by x.avaliadaEm desc")
    List<ExposicaoAmbiental> findDaEmergencia(UUID emergenciaId);
}
