package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoMassa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventoMassaRepository extends JpaRepository<EventoMassa, UUID> {

    Optional<EventoMassa> findByEmergencia_Uuid(UUID emergenciaId);
}
