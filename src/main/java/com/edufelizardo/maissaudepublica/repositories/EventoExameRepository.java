package com.edufelizardo.maissaudepublica.repositories;

import com.edufelizardo.maissaudepublica.models.EventoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoExameRepository extends JpaRepository<EventoExame, UUID> {
    List<EventoExame> findByPedido_UuidOrderByOcorridoEmAsc(UUID pedidoId);
}
