package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.RelatorioPosEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RelatorioPosEventoRepository extends JpaRepository<RelatorioPosEvento, UUID> {

    Optional<RelatorioPosEvento> findByEmergencia_Uuid(UUID emergenciaId);
}
